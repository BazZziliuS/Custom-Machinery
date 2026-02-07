package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public class RecipeChecker<T extends Recipe<?> & IMachineRecipe> {

    private final RecipeHolder<T> recipe;
    private final List<RecipeRequirement<?, ?>> inventoryRequirements;
    private final List<RecipeRequirement<?, ?>> checkedInventoryRequirements;
    private final List<RecipeRequirement<?, ?>> worldRequirements;
    private final boolean isInventoryRequirementsOnly;
    private boolean inventoryRequirementsOk = false;

    public RecipeChecker(RecipeHolder<T> recipe) {
        this.recipe = recipe;
        this.inventoryRequirements = recipe.value().getRequirements().stream().filter(r -> !r.getType().isWorldRequirement()).toList();
        this.checkedInventoryRequirements = new ArrayList<>();
        this.worldRequirements = recipe.value().getRequirements().stream().filter(r -> r.getType().isWorldRequirement()).toList();
        this.isInventoryRequirementsOnly = recipe.value().getRequirements().stream().noneMatch(r -> r.getType().isWorldRequirement());
    }

    public boolean check(MachineTile tile, ICraftingContext context, boolean inventoryChanged) {
        if (inventoryChanged) {
            this.checkedInventoryRequirements.clear();
            this.inventoryRequirementsOk = false;

            for (RecipeRequirement<?, ?> requirement : this.inventoryRequirements) {
                if (this.checkedInventoryRequirements.contains(requirement))
                    continue;
                this.checkedInventoryRequirements.add(requirement);
                if (!checkRequirement(requirement, tile, context))
                    return false;
            }

            this.inventoryRequirementsOk = true;
        }

        if (!this.inventoryRequirementsOk)
            return false;

        for(int i = 0; i < this.worldRequirements.size(); i++) {
            if(!checkRequirement(this.worldRequirements.get(i), tile, context))
                return false;
        }
        return true;
    }

    public RecipeHolder<T> getRecipe() {
        return this.recipe;
    }

    public boolean isInventoryRequirementsOnly() {
        return this.isInventoryRequirementsOnly;
    }

    public boolean isInventoryRequirementsOk() {
        return this.inventoryRequirementsOk;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean checkRequirement(RecipeRequirement<?, ?> requirement, MachineTile tile, ICraftingContext context) {
        IMachineComponent component = tile.getComponentManager().getComponentDirect(requirement.requirement().getComponentType());
        if(component == null)
            return false;
        return ((IRequirement) requirement.requirement()).test(component, context);
    }
}
