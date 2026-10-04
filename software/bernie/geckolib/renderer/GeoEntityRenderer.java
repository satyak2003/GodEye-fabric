package software.bernie.geckolib.renderer;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.entity.*;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.GeckoLibClientServices;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayersContainer;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.MiscUtil;

import java.util.List;
import java.util.function.Function;

/**
 * Base {@link GeoRenderer} class for rendering {@link Entity Entities} specifically
 * <p>
 * All entities added to be rendered by GeckoLib should use an instance of this class
 * <p>
 * This also includes {@link net.minecraft.entity.projectile.ProjectileEntity Projectiles}
 *
 * @param <T> Entity animatable class type
 * @param <R> RenderState class type
 */
public class GeoEntityRenderer<T extends Entity & GeoAnimatable, R extends EntityRenderState & GeoRenderState> extends EntityRenderer<T, R> implements GeoRenderer<T, Void, R> {
	protected final GeoRenderLayersContainer<T, Void, R> renderLayers = new GeoRenderLayersContainer<>(this);
	protected final GeoModel<T> model;
	protected final ItemModelManager itemModelResolver;

	protected float scaleWidth = 1;
	protected float scaleHeight = 1;

    /**
     * Creates a new defaulted renderer instance, using the entity's registered id as the file name for its assets
     */
    public GeoEntityRenderer(EntityRendererFactory.Context context, EntityType<? extends T> entityType) {
        this(context, new DefaultedEntityGeoModel<>(Registries.ENTITY_TYPE.getId(entityType)));
    }

	public GeoEntityRenderer(EntityRendererFactory.Context context, GeoModel<T> model) {
		super(context);

		this.model = model;
		this.itemModelResolver = context.getItemModelManager();
	}

	/**
	 * Get the maximum distance (in blocks) that an entity's nameplate should be visible when it is sneaking
	 * <p>
	 * This is only a short-circuit predicate, and other conditions after this check must be also passed in order for the name to render
	 * <p>
	 * This is hard-capped at a maximum of 256 blocks regardless of what this method returns
	 */
	public double getNameRenderCutoffDistance(T animatable) {
		return 32d;
	}

    /**
     * Returns the max rotation value for dying entities
     * <p>
     * You might want to modify this for different aesthetics, such as a {@link SpiderEntity} flipping upside down on death
     * <p>
     * Functionally equivalent to {@code LivingEntityRenderer#getFlipDegrees}
     */
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 90f;
    }

    /**
     * Makes a covariant variable of the given {@link GeoRenderState} and {@link LivingEntityRenderState}
     * (Essentially a variable that is <b>both</b> types) for ease of use
     * <p>
     * Because of the lack of extensibility in covariant return types, a new version of this method needs to be made
     * for any other covariant combination
     *
     * @param renderState The base GeoRenderState to cast
     * @return The GeoRenderState cast <i>additively</i> as a LivingEntityRenderState
     */
    @SuppressWarnings("unchecked")
    protected <S extends LivingEntityRenderState & GeoRenderState> S convertRenderStateToLiving(R renderState) {
        return (S)renderState;
    }

    //<editor-fold defaultstate="collapsed" desc="<Internal Methods>">
    /**
     * Gets the model instance for this renderer
     */
    @Override
    public GeoModel<T> getGeoModel() {
        return this.model;
    }

    /**
     * Returns the list of registered {@link GeoRenderLayer GeoRenderLayers} for this renderer
     */
    @Override
    public List<GeoRenderLayer<T, Void, R>> getRenderLayers() {
        return this.renderLayers.getRenderLayers();
    }

    /**
     * Adds a {@link GeoRenderLayer} to this renderer, to be called after the main model is rendered each frame
     */
    @SuppressWarnings("UnusedReturnValue")
    public GeoEntityRenderer<T, R> withRenderLayer(Function<? super GeoEntityRenderer<T, R>, GeoRenderLayer<T, Void, R>> renderLayer) {
        return withRenderLayer(renderLayer.apply(this));
    }

    /**
     * Adds a {@link GeoRenderLayer} to this renderer, to be called after the main model is rendered each frame
     */
    public GeoEntityRenderer<T, R> withRenderLayer(GeoRenderLayer<T, Void, R> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    /**
     * Sets a scale override for this renderer, telling GeckoLib to pre-scale the model
     */
    public GeoEntityRenderer<T, R> withScale(float scale) {
        return withScale(scale, scale);
    }

    /**
     * Sets a scale override for this renderer, telling GeckoLib to pre-scale the model
     */
    public GeoEntityRenderer<T, R> withScale(float scaleWidth, float scaleHeight) {
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;

        return this;
    }

    /**
     * Gets the id that represents the current animatable's instance for animation purposes.
     *
     * @param animatable The Animatable instance being renderer
     */
    @ApiStatus.OverrideOnly
    @Override
    public long getInstanceId(T animatable, @Nullable Void ignored) {
        return animatable.getId();
    }

    /**
     * Gets a tint-applying color to render the given animatable with
     * <p>
     * Returns opaque white by default, modified for invisibility in spectator
     */
    @Override
    public int getRenderColor(T animatable, @Nullable Void relatedObject, float partialTick) {
        int color = GeoRenderer.super.getRenderColor(animatable, relatedObject, partialTick);
        PlayerEntity player = ClientUtil.getClientPlayer();

        if (animatable.isInvisible() && player != null && !animatable.isInvisibleTo(player))
            color = ColorHelper.withAlpha(MathHelper.ceil(ColorHelper.getAlpha(color) * 38 / 255f), color);

        return color;
    }

    /**
     * Gets a packed overlay coordinate pair for rendering
     * <p>
     * Mostly just used for the red tint when an entity is hurt,
     * but can be used for other things like the {@link net.minecraft.entity.mob.CreeperEntity}
     * white tint when exploding.
     */
    @Override
    public int getPackedOverlay(T animatable, @Nullable Void relatedObject, float u, float partialTick) {
        if (!(animatable instanceof LivingEntity entity))
            return OverlayTexture.DEFAULT_UV;

        return OverlayTexture.packUv(OverlayTexture.getU(u),
                                   OverlayTexture.getV(entity.hurtTime > 0 || entity.deathTime > 0));
    }

    /**
     * Whether the entity's nametag should be rendered or not
     * <p>
     * Used to determine nametag attachment in {@link EntityRenderer#updateRenderState(Entity, EntityRenderState, float)}
     */
    @Override
    public boolean hasLabel(T animatable, double distToCameraSq) {
        if (!(animatable instanceof LivingEntity))
            return super.hasLabel(animatable, distToCameraSq);

        if (animatable.isSneaky()) {
            double nameRenderCutoff = getNameRenderCutoffDistance(animatable);

            if (distToCameraSq >= nameRenderCutoff * nameRenderCutoff)
                return false;
        }

        if (animatable instanceof MobEntity && (!animatable.shouldRenderName() && (!animatable.hasCustomName() || animatable != this.dispatcher.targetedEntity)))
            return false;

        final MinecraftClient minecraft = MinecraftClient.getInstance();
        final PlayerEntity player = ClientUtil.getClientPlayer();
        boolean visibleToClient = player != null && !animatable.isInvisibleTo(player);
        AbstractTeam entityTeam = animatable.getScoreboardTeam();

        if (player == null || entityTeam == null)
            return MinecraftClient.isHudEnabled() && animatable != minecraft.getCameraEntity() && visibleToClient && !animatable.hasPassengers();

        AbstractTeam playerTeam = ClientUtil.getClientPlayer().getScoreboardTeam();

        return switch (entityTeam.getNameTagVisibilityRule()) {
            case ALWAYS -> visibleToClient;
            case NEVER -> false;
            case HIDE_FOR_OTHER_TEAMS -> playerTeam == null ? visibleToClient : entityTeam.isEqual(playerTeam) && (entityTeam.shouldShowFriendlyInvisibles() || visibleToClient);
            case HIDE_FOR_OWN_TEAM -> playerTeam == null ? visibleToClient : !entityTeam.isEqual(playerTeam) && visibleToClient;
        };
    }

    /**
     * Calculate the yaw of the given animatable.
     * <p>
     * Normally only called for non-{@link LivingEntity LivingEntities}, and shouldn't be considered a safe place to modify rotation<br>
     * Do that in {@link software.bernie.geckolib.renderer.base.GeoRendererInternals#addRenderData(GeoAnimatable, Object, GeoRenderState, float)} instead
     */
    protected float calculateYRot(T animatable, float yHeadRot, float partialTick) {
        if (!(animatable.getVehicle() instanceof LivingEntity vehicle))
            return animatable instanceof LivingEntity livingEntity ? MathHelper.lerpAngleDegrees(partialTick, livingEntity.lastBodyYaw, livingEntity.bodyYaw) : animatable.getBodyYaw();

        float vehicleRotation = MathHelper.lerpAngleDegrees(partialTick, vehicle.lastBodyYaw, vehicle.bodyYaw);
        float clampedVehicleRotation = MathHelper.clamp(MathHelper.wrapDegrees(-vehicleRotation), -85, 85);
        vehicleRotation = yHeadRot - vehicleRotation;

        if (Math.abs(clampedVehicleRotation) > 50)
            vehicleRotation += clampedVehicleRotation * 0.2f;

        return vehicleRotation;
    }

    /**
     * Gets the {@link RenderLayer} to render the current render pass with
     * <p>
     * Uses the {@link RenderLayers#entityCutoutNoCull} {@code RenderType} by default
     * <p>
     * Override this to change the way a model will render (such as translucent models, etc.)
     *
     * @return Return the RenderType to use, or null to prevent the model rendering. Returning null will not prevent animation functions from taking place
     */
    @Override
    public @Nullable RenderLayer getRenderType(R renderState, Identifier texture) {
        if (renderState.invisible && !renderState.getOrDefaultGeckolibData(DataTickets.INVISIBLE_TO_PLAYER, false))
            return RenderLayers.itemEntityTranslucentCull(texture);

        if (!renderState.invisible)
            return GeoRenderer.super.getRenderType(renderState, texture);

        return renderState.hasOutline() ? RenderLayers.outlineNoCull(texture) : null;
    }

    /**
     * Internal method for capturing the common RenderState data for all animatable objects
     */
    @ApiStatus.Internal
    @Override
    public void captureDefaultRenderState(T animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        GeoRenderer.super.captureDefaultRenderState(animatable, relatedObject, renderState, partialTick);

        renderState.addGeckolibData(DataTickets.VELOCITY, animatable.getVelocity());
        renderState.addGeckolibData(DataTickets.BLOCKPOS, animatable.getBlockPos());
        renderState.addGeckolibData(DataTickets.SPRINTING, animatable.isSprinting());
        renderState.addGeckolibData(DataTickets.IS_CROUCHING, animatable.isInSneakingPose());
        renderState.addGeckolibData(DataTickets.POSITION, animatable.getEntityPos());
        renderState.addGeckolibData(DataTickets.IS_MOVING, (animatable instanceof LivingEntity livingEntity ? livingEntity.limbAnimator.getSpeed() : animatable.getVelocity().lengthSquared()) >= getMotionAnimThreshold(animatable));

        if (animatable instanceof LivingEntity livingEntity) {
            renderState.addGeckolibData(DataTickets.SWINGING_ARM, livingEntity.handSwinging);
            renderState.addGeckolibData(DataTickets.IS_DEAD_OR_DYING, livingEntity.isDead());
        }

        if (!(renderState instanceof LivingEntityRenderState)) {
            renderState.addGeckolibData(DataTickets.INVISIBLE_TO_PLAYER, animatable.isInvisible() && (ClientUtil.getClientPlayer() == null || animatable.isInvisibleTo(ClientUtil.getClientPlayer())));
            renderState.addGeckolibData(DataTickets.IS_SHAKING, animatable.isFrozen());
            renderState.addGeckolibData(DataTickets.ENTITY_POSE, animatable.getPose());
            renderState.addGeckolibData(DataTickets.ENTITY_PITCH, animatable.getLerpedPitch(partialTick));
            renderState.addGeckolibData(DataTickets.ENTITY_YAW, calculateYRot(animatable, 0, partialTick));
            renderState.addGeckolibData(DataTickets.ENTITY_BODY_YAW, renderState.getOrDefaultGeckolibData(DataTickets.ENTITY_YAW, 0f));
        }
    }

    /**
     * Scales the {@link MatrixStack} in preparation for rendering the model, excluding when re-rendering the model as part of a {@link GeoRenderLayer} or external render call
     * <p>
     * Override and call {@code super} with modified scale values as needed to further modify the scale of the model
     */
    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo, float widthScale, float heightScale) {
        float nativeScale = renderPassInfo.renderState() instanceof LivingEntityRenderState livingRenderState ? livingRenderState.baseScale : 1;

        GeoRenderer.super.scaleModelForRender(renderPassInfo, widthScale * this.scaleWidth * nativeScale, heightScale * this.scaleHeight * nativeScale);
    }

    /**
     * Transform the {@link MatrixStack} in preparation for rendering the model.
     * <p>
     * This is called after {@link #scaleModelForRender}, and so any transformations here will be scaled appropriately.
     * If you need to do pre-scale translations, use {@link #preRenderPass}
     * <p>
     * PoseStack translations made here are kept until the end of the render process
     */
    @Override
    public void adjustRenderPose(RenderPassInfo<R> renderPassInfo) {
        final R renderState = renderPassInfo.renderState();
        final LivingEntityRenderState livingRenderState = renderState instanceof LivingEntityRenderState state ? state : null;
        final MatrixStack poseStack = renderPassInfo.poseStack();

        if (livingRenderState != null && renderState.getGeckolibData(DataTickets.ENTITY_POSE) == EntityPose.SLEEPING) {
            Direction bedDirection = livingRenderState.sleepingDirection;

            if (bedDirection != null) {
                float eyePosOffset = livingRenderState.standingEyeHeight - 0.1F;

                poseStack.translate(-bedDirection.getOffsetX() * eyePosOffset, 0, -bedDirection.getOffsetZ() * eyePosOffset);
            }
        }

        applyRotations(renderPassInfo, poseStack, livingRenderState != null ? livingRenderState.baseScale : 1);
        poseStack.translate(0, 0.01f, 0);
    }

    /**
     * Initial access point for vanilla's {@link GeoEntityRenderer} class<br>
     * Immediately defers to {@link GeoRenderer#performRenderPass(GeoRenderState, MatrixStack, OrderedRenderCommandQueue, CameraRenderState)}
     */
    @ApiStatus.Internal
    @Override
    public void render(R renderState, MatrixStack poseStack, OrderedRenderCommandQueue renderTasks, CameraRenderState cameraState) {
        GeoRenderer.super.performRenderPass(renderState, poseStack, renderTasks, cameraState);
    }

    /**
     * Called after the rest of the render pass has completed, including discarding the PoseStack's pose.
     * <p>
     * The actual rendering of the object has not yet taken place, as that is done in a deferred {@link #performRenderPass submission}
     */
    @Override
    public void postRenderPass(RenderPassInfo<R> renderPassInfo, OrderedRenderCommandQueue renderTasks) {
        super.render(renderPassInfo.renderState(), renderPassInfo.poseStack(), renderTasks, renderPassInfo.cameraState());
    }

    /**
     * Applies rotation transformations to the renderer prior to render time to account for various entity states
     */
    protected void applyRotations(RenderPassInfo<R> renderPassInfo, MatrixStack poseStack, float nativeScale) {
        final R renderState = renderPassInfo.renderState();
        float rotationYaw = renderState.getOrDefaultGeckolibData(DataTickets.ENTITY_BODY_YAW, 0f);

        if (renderState.getOrDefaultGeckolibData(DataTickets.IS_SHAKING, false))
            rotationYaw += (float)(Math.cos(renderState.age * 3.25d) * Math.PI * 0.4d);

        boolean sleeping = renderState.getGeckolibData(DataTickets.ENTITY_POSE) == EntityPose.SLEEPING;

        if (!sleeping)
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - rotationYaw));

        if (renderState instanceof LivingEntityRenderState livingRenderState) {
            if (livingRenderState.deathTime > 0) {
                poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(Math.min(MathHelper.sqrt((livingRenderState.deathTime - 1f) / 20f * 1.6f), 1) * getDeathMaxRotation(renderState)));
            }
            else if (livingRenderState.usingRiptide) {
                poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90f - livingRenderState.pitch));
                poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(renderState.age * -75f));
            }
            else if (sleeping) {
                Direction bedOrientation = livingRenderState.sleepingDirection;

                poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(bedOrientation != null ? MiscUtil.getDirectionAngle(bedOrientation) : rotationYaw));
                poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(getDeathMaxRotation(renderState)));
                poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270f));
            }
            else if (livingRenderState.flipUpsideDown) {
                poseStack.translate(0, (livingRenderState.height + 0.1f) / nativeScale, 0);
                poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f));
            }
        }
    }

    /**
     * Create the base (blank) {@link R renderState} instance for this renderer.
     * <p>
     * By default, it is an {@link EntityRenderState}, or a {@link LivingEntityRenderState} if the entity is an instance of {@link LivingEntity}<br>
     * All EntityRenderStates of any kind are automatically {@link GeoRenderState}s
     * <p>
     * Override this if you want to utilize a different subclass of EntityRenderState
     */
    @SuppressWarnings("unchecked")
    @Override
    public R createRenderState(T animatable, @Nullable Void relatedObject) {
        return (R)(animatable instanceof LivingEntity ? new LivingEntityRenderState() : new EntityRenderState());
    }

    /**
     * Create the contextually relevant {@link EntityRenderState} for the current render pass
     * <p>
     * GeckoLib also uses this to dynamically handle the default EntityRenderState setup
     * <p>
     * If overriding this for a custom RenderState, ensure you call {@code super} first
     */
    @ApiStatus.Internal
    @Override
    public final R getAndUpdateRenderState(T entity, float partialTick) {
        R renderState = createRenderState(entity, null);

        updateRenderState(entity, renderState, partialTick);
        updateShadow(entity, renderState);

        return renderState;
    }

    /**
     * Fill the EntityRenderState for the current render pass.
     * <p>
     * You should only be overriding this if you have extended the {@link R renderState} type.<br>
     * If you're just adding GeckoLib rendering data, you should be using {@link software.bernie.geckolib.renderer.base.GeoRendererInternals#addRenderData(GeoAnimatable, Object, GeoRenderState, float)} instead
     */
    @ApiStatus.OverrideOnly
    @Override
    public void updateRenderState(T entity, R entityRenderState, float partialTick) {
        super.updateRenderState(entity, entityRenderState, partialTick);

        if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState)
            extractLivingEntityRenderState((LivingEntity)entity, livingEntityRenderState, partialTick, this.itemModelResolver);

        fillRenderState(entity, null, entityRenderState, partialTick);
    }

    /**
     * Replica of {@link LivingEntityRenderer#updateRenderState(LivingEntity, LivingEntityRenderState, float)}.
     * <p>
     * This is only called if the entity for this renderer is a {@link LivingEntity}
     */
    protected void extractLivingEntityRenderState(LivingEntity entity, LivingEntityRenderState renderState, float partialTick, ItemModelManager itemModelResolver) {
        final MinecraftClient minecraft = MinecraftClient.getInstance();
        final float lerpHeadYRot = MathHelper.lerpAngleDegrees(partialTick, entity.lastHeadYaw, entity.headYaw);
        final Text customName = entity.getCustomName();
        final ItemStack helmetStack = entity.getEquippedStack(EquipmentSlot.HEAD);

        renderState.bodyYaw = LivingEntityRenderer.clampBodyYaw(entity, lerpHeadYRot, partialTick);
        renderState.relativeHeadYaw = MathHelper.wrapDegrees(lerpHeadYRot - renderState.bodyYaw);
        renderState.pitch = entity.getLerpedPitch(partialTick);
        renderState.flipUpsideDown = customName != null && LivingEntityRenderer.shouldFlipUpsideDown(customName.getString());

        if (renderState.flipUpsideDown) {
            renderState.pitch *= -1;
            renderState.relativeHeadYaw *= -1;
        }

        if (!entity.hasVehicle() && entity.isAlive()) {
            renderState.limbSwingAnimationProgress = entity.limbAnimator.getAnimationProgress(partialTick);
            renderState.limbSwingAmplitude = entity.limbAnimator.getAmplitude(partialTick);
        }
        else {
            renderState.limbSwingAnimationProgress = 0;
            renderState.limbSwingAmplitude = 0;
        }

        if (entity.getVehicle() instanceof LivingEntity vehicle) {
            renderState.headItemAnimationProgress = vehicle.limbAnimator.getAnimationProgress(partialTick);
        }
        else {
            renderState.headItemAnimationProgress = renderState.limbSwingAnimationProgress;
        }

        renderState.baseScale = entity.getScale();
        renderState.ageScale = entity.getScaleFactor();
        renderState.pose = entity.getPose();
        renderState.sleepingDirection = entity.getSleepingDirection();

        if (renderState.sleepingDirection != null)
            renderState.standingEyeHeight = entity.getEyeHeight(EntityPose.STANDING);

        renderState.shaking = entity.isFrozen();
        renderState.baby = entity.isBaby();
        renderState.touchingWater = entity.isTouchingWater();
        renderState.usingRiptide = entity.isUsingRiptide();
        renderState.hurt = entity.hurtTime > 0 || entity.deathTime > 0;

        if (helmetStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock skullBlock) {
            renderState.wearingSkullType = skullBlock.getSkullType();
            renderState.wearingSkullProfile = helmetStack.get(DataComponentTypes.PROFILE);
            renderState.headItemRenderState.clear();
        }
        else {
            renderState.wearingSkullType = null;
            renderState.wearingSkullProfile = null;

            if (!ArmorFeatureRenderer.hasModel(helmetStack, EquipmentSlot.HEAD)) {
                this.itemModelResolver.updateForLivingEntity(renderState.headItemRenderState, helmetStack, ItemDisplayContext.HEAD, entity);
            }
            else {
                renderState.headItemRenderState.clear();
            }
        }

        renderState.deathTime = entity.deathTime > 0 ? (float)entity.deathTime + partialTick : 0;
        renderState.invisibleToPlayer = renderState.invisible && minecraft.player != null && entity.isInvisibleTo(minecraft.player);
    }

    /**
     * Create and fire the relevant {@code CompileLayers} event hook for this renderer
     */
    @Override
    public void fireCompileRenderLayersEvent() {
        GeckoLibClientServices.EVENTS.fireCompileEntityRenderLayers(this);
    }

    /**
     * Create and fire the relevant {@code CompileRenderState} event hook for this renderer
     */
    @Override
    public void fireCompileRenderStateEvent(T animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        GeckoLibClientServices.EVENTS.fireCompileEntityRenderState(this, renderState, animatable);
    }

    /**
     * Create and fire the relevant {@code Pre-Render} event hook for this renderer
     *
     * @return Whether the renderer should proceed based on the cancellation state of the event
     */
    @Override
    public boolean firePreRenderEvent(RenderPassInfo<R> renderPassInfo, OrderedRenderCommandQueue renderTasks) {
        return GeckoLibClientServices.EVENTS.fireEntityPreRender(renderPassInfo, renderTasks);
    }

    /**
     * @deprecated GeckoLib defers creation of this to allow for dynamic handling in {@link #updateRenderState(Entity, EntityRenderState, float)}
     */
    @Deprecated
    @ApiStatus.Internal
    @Override
    public final @Nullable R createRenderState() {
        return null;
    }
    //</editor-fold>
}
