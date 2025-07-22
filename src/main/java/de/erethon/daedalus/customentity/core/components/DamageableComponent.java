package de.erethon.daedalus.customentity.core.components;

import de.erethon.daedalus.customentity.ModeledEntity;
import de.erethon.daedalus.customentity.core.OBBHitDetection;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a component that can handle damage interactions, including applying damage from various sources
 * and managing the internal health of an associated modeled entity. This component is responsible for processing
 * both custom damage and Minecraft's native damage system, depending on the context and the underlying entity type.
 */
public class DamageableComponent {

    private final ModeledEntity modeledEntity;

    private final boolean internallyImmortal = false;

    private double internalHealth = 1;

    public DamageableComponent(ModeledEntity modeledEntity) {
        this.modeledEntity = modeledEntity;
    }

    private void handleNonLivingEntityDamage(double amount) {
        if (!internallyImmortal) {
            internalHealth -= amount;
            if (internalHealth <= 0) {
                modeledEntity.removeWithDeathAnimation();
            }
        }
        modeledEntity.getSkeleton().tint();
    }

    public void damage(double amount) {
        if (modeledEntity.getUnderlyingEntity() instanceof LivingEntity livingEntity) {
            OBBHitDetection.applyDamage = true;
            livingEntity.damage(amount);
            OBBHitDetection.applyDamage = false;
        } else {
            handleNonLivingEntityDamage(amount);
        }
        modeledEntity.getSkeleton().tint();
    }

    public void damage(Entity damager, double amount) {
        if (modeledEntity.getUnderlyingEntity() instanceof LivingEntity livingEntity) {
            livingEntity.damage(amount, damager);
        } else {
            handleNonLivingEntityDamage(amount);
        }
        modeledEntity.getSkeleton().tint();
    }

    public void damage(Entity damager) {
        if (modeledEntity.getUnderlyingEntity() instanceof LivingEntity livingEntity &&
                !livingEntity.getType().equals(EntityType.ARMOR_STAND) &&
                damager instanceof LivingEntity damagerLivingEntity) {
            damagerLivingEntity.attack(livingEntity);
        } else {
            handleNonLivingEntityDamage(1);
        }
        modeledEntity.getSkeleton().tint();
    }

    public boolean damage(Projectile projectile) {
        double damage = 0;

        if (projectile.getShooter() != null && projectile.getShooter().equals(modeledEntity.getUnderlyingEntity()))
            return false;

        if (!(projectile instanceof Arrow arrow)) return false;

        double speed = arrow.getVelocity().length();
        damage = Math.ceil(speed * arrow.getDamage());

        if (arrow.getShooter() instanceof LivingEntity shooter) {
            ItemStack bow = arrow.getWeapon();
            if (bow != null && bow.containsEnchantment(Enchantment.POWER)) {
                int level = bow.getEnchantmentLevel(Enchantment.POWER);
                double bonus = Math.ceil(0.25 * (level + 1) * damage);
                damage += bonus;
            }
        }

        if (projectile.getShooter() instanceof LivingEntity damager) {
            damage(damager, damage);
        } else {
            damage((Entity) projectile.getShooter(), damage);
        }
        modeledEntity.getSkeleton().tint();
        return true;
    }

    /**
     * This is the preferred way to attack a living entity if you have a living entity as the underlying entity.
     * It will simulate a real attack using Minecraft's damage system. If you do not have a living entity as the underlying entity,
     * it will default to doing 1 damage to the target, and at that point, you should use {@link #attack(LivingEntity, double)} instead.
     */
    public void attack(LivingEntity target) {
        Attribute attribute = Attribute.ATTACK_DAMAGE;
        if (modeledEntity.getUnderlyingEntity() instanceof LivingEntity underlyingLivingEntity &&
                attribute != null &&
                underlyingLivingEntity.getAttribute(attribute) != null) {
            OBBHitDetection.applyDamage = true;
            underlyingLivingEntity.attack(target);
            OBBHitDetection.applyDamage = false;
        } else {
            OBBHitDetection.applyDamage = true;
            target.damage(2, modeledEntity.getUnderlyingEntity());
            OBBHitDetection.applyDamage = false;
        }
    }

    /**
     * Beware, this damage uses custom damage which might get reduced somewhat randomly by Minecraft, so test it before using it.
     * Preferably, you should use {@link #attack(LivingEntity)} instead, and if you need to modify the damage, you can
     * hijack the damage event and modify the damage amount.
     *
     * @param target Target to attack
     * @param damage Damage to deal
     */
    public void attack(LivingEntity target, double damage) {
        OBBHitDetection.applyDamage = true;
        target.damage(damage, modeledEntity.getUnderlyingEntity());
        OBBHitDetection.applyDamage = false;
    }

    public double getInternalHealth() {
        return internalHealth;
    }

    public void setInternalHealth(double internalHealth) {
        this.internalHealth = internalHealth;
    }

    public ModeledEntity getModeledEntity() {
        return modeledEntity;
    }

    public boolean isInternallyImmortal() {
        return internallyImmortal;
    }

}
