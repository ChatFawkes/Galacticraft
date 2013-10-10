package micdoodle8.mods.galacticraft.core.nei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import codechicken.core.gui.GuiDraw;
import codechicken.nei.ItemList;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.FurnaceRecipeHandler.FuelPair;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class IngotCompressorRecipeHandler extends TemplateRecipeHandler
{
    private static final ResourceLocation ingotCompressorTexture = new ResourceLocation(GalacticraftCore.TEXTURE_DOMAIN, "textures/gui/ingotCompressor.png");
    int ticksPassed;
    public static ArrayList<FuelPair> afuels;
    public static TreeSet<Integer> efuels;

    public String getRecipeId()
    {
        return "galacticraft.ingotcompressor";
    }

    @Override
    public int recipiesPerPage()
    {
        return 1;
    }

    public Set<Entry<ArrayList<PositionedStack>, PositionedStack>> getRecipes()
    {
        HashMap<ArrayList<PositionedStack>, PositionedStack> recipes = new HashMap<ArrayList<PositionedStack>, PositionedStack>();
        
        for (Entry<HashMap<Integer, PositionedStack>, PositionedStack> stack : NEIGalacticraftConfig.getIngotCompressorRecipes())
        {
            ArrayList<PositionedStack> inputStacks = new ArrayList<PositionedStack>();
            
            for (Map.Entry<Integer, PositionedStack> input : stack.getKey().entrySet())
            {
                inputStacks.add(input.getValue());
            }
            
            recipes.put(inputStacks, stack.getValue());
        }
        
        return recipes.entrySet();
    }

    @Override
    public void drawBackground(int i)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiDraw.changeTexture(IngotCompressorRecipeHandler.ingotCompressorTexture);
        GuiDraw.drawTexturedModalRect(20, 25, 18, 17, 137, 78);
        
        if (this.ticksPassed % 70 > 26)
        {
            GuiDraw.drawTexturedModalRect(103, 36, 176, 0, 17, 13);
        }

        GuiDraw.drawTexturedModalRect(79, 44, 176, 13, Math.min(this.ticksPassed % 70, 53), 17);
        
        int yOffset = (int) Math.floor((this.ticksPassed % 48) * 0.29166666666666666666666666666667D);
        
        GuiDraw.drawTexturedModalRect(83, 35 + yOffset, 176, 30 + yOffset, 14, 14 - yOffset);
    }

    @Override
    public void onUpdate()
    {
        this.ticksPassed += 1;
        super.onUpdate();
    }

    @Override
    public void loadTransferRects()
    {
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if (outputId.equals(this.getRecipeId()))
        {
            for (final Map.Entry<ArrayList<PositionedStack>, PositionedStack> irecipe : this.getRecipes())
            {
                this.arecipes.add(new CompressorRecipe(irecipe));
            }
        }
        else
        {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
        for (final Map.Entry<ArrayList<PositionedStack>, PositionedStack> irecipe : this.getRecipes())
        {
            if (NEIServerUtils.areStacksSameTypeCrafting(irecipe.getValue().item, result))
            {
                this.arecipes.add(new CompressorRecipe(irecipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
        for (final Map.Entry<ArrayList<PositionedStack>, PositionedStack> irecipe : this.getRecipes())
        {
            for (final PositionedStack pstack : irecipe.getKey())
            {
                if (NEIServerUtils.areStacksSameTypeCrafting(ingredient, pstack.item))
                {
                    this.arecipes.add(new CompressorRecipe(irecipe));
                    break;
                }
            }
        }
    }
    
    @Override
    public TemplateRecipeHandler newInstance()
    {
        if (afuels == null)
        {
            findFuels();
        }
        
        return super.newInstance();
    }

    @Override
    public ArrayList<PositionedStack> getIngredientStacks(int recipe)
    {
        return (ArrayList<PositionedStack>) this.arecipes.get(recipe).getIngredients();
    }

    @Override
    public PositionedStack getResultStack(int recipe)
    {
        if (this.ticksPassed % 70 >= 53)
        {
            return this.arecipes.get(recipe).getResult();
        }
        
        return null;
    }
    
    private static void removeFuels()
    {
        efuels = new TreeSet<Integer>();
        efuels.add(Block.mushroomCapBrown.blockID);
        efuels.add(Block.mushroomCapRed.blockID);
        efuels.add(Block.signPost.blockID);
        efuels.add(Block.signWall.blockID);
        efuels.add(Block.doorWood.blockID);
        efuels.add(Block.lockedChest.blockID);
    }
    
    private static void findFuels()
    {        
        afuels = new ArrayList<FuelPair>();
        for(ItemStack item : ItemList.items)
        {
            if(!efuels.contains(item.itemID))
            {
                int burnTime = TileEntityFurnace.getItemBurnTime(item);
                if(burnTime > 0)
                {
                    FuelPair fuelPair = new FuelPair(item.copy(), burnTime);
                    fuelPair.stack.relx = 57;
                    fuelPair.stack.rely = 83;
                    afuels.add(fuelPair);
                }
            }
        }
    }

    public class CompressorRecipe extends TemplateRecipeHandler.CachedRecipe
    {
        public ArrayList<PositionedStack> input;
        public PositionedStack output;

        @Override
        public ArrayList<PositionedStack> getIngredients()
        {
            if (ElectricIngotCompressorRecipeHandler.ticksPassed % 70 >= 53)
            {
                return new ArrayList<PositionedStack>();
            }

            return this.input;
        }

        @Override
        public PositionedStack getResult()
        {
            return this.output;
        }

        public CompressorRecipe(ArrayList<PositionedStack> pstack1, PositionedStack pstack2)
        {
            super();
            this.input = pstack1;
            this.output = pstack2;
        }

        public CompressorRecipe(Map.Entry<ArrayList<PositionedStack>, PositionedStack> recipe)
        {
            this(recipe.getKey(), recipe.getValue());
        }

        public List<PositionedStack> getOtherStacks()
        {
            ArrayList<PositionedStack> stacks = new ArrayList<PositionedStack>();
            PositionedStack stack = getOtherStack();
            if(stack != null)
                stacks.add(stack);
            return stacks;
        }
        
        @Override
        public PositionedStack getOtherStack()
        {
            return afuels.get((ticksPassed / 48) % afuels.size()).stack;
        }
    }

    @Override
    public String getRecipeName()
    {
        return "Ingot Compressor";
    }

    @Override
    public String getGuiTexture()
    {
        return "/assets/galacticraftcore/textures/gui/ingotCompressor.png";
    }
    
    static
    {
        removeFuels();
    }
}
