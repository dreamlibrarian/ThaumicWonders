package com.verdantartifice.thaumicwonders.common.blocks.devices;

import java.util.Random;

import com.verdantartifice.thaumicwonders.common.blocks.base.BlockTW;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aura.AuraHelper;

public class BlockFluxCapacitor extends BlockTW {
    public static final PropertyInteger CHARGE = PropertyInteger.create("charge", 0, 10);
    
    public BlockFluxCapacitor() {
        super(Material.ROCK, "flux_capacitor");
        this.setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CHARGE, Integer.valueOf(0)));
    }
    
    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            int charge = this.getMetaFromState(state);
            if (worldIn.isBlockPowered(pos)) {
                if (charge > 0) {
                    AuraHelper.polluteAura(worldIn, pos, 1.0F, true);
                    worldIn.setBlockState(pos, state.withProperty(CHARGE, Integer.valueOf(charge - 1)));
                    worldIn.scheduleUpdate(pos, state.getBlock(), 5);
                }
            } else {
                float flux = AuraHelper.getFlux(worldIn, pos);
                if (charge < 10 && flux >= 1.0F) {
                    AuraHelper.drainFlux(worldIn, pos, 1.0F, false);
                    worldIn.setBlockState(pos, state.withProperty(CHARGE, Integer.valueOf(charge + 1)));
                    worldIn.scheduleUpdate(pos, state.getBlock(), 100 + rand.nextInt(100));
                }
            }
        }
    }
    
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (worldIn.isBlockPowered(pos)) {
            worldIn.scheduleUpdate(pos, this, 1);
        }
    }
    
    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }
    
    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        return this.getMetaFromState(blockState);
    }
    
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getBlock().getMetaFromState(state);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        int i = source.getCombinedLight(pos, state.getLightValue(source, pos));
        int j = 180;
        int k = i & 0xFF;
        int l = j & 0xFF;
        int i1 = i >> 16 & 0xFF;
        int j1 = j >> 16 & 0xFF;
        return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { CHARGE });
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(CHARGE, Integer.valueOf(meta));
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(CHARGE).intValue();
    }
    
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        ItemStack drop = new ItemStack(this);
        if (!drop.hasTagCompound()) {
            drop.setTagCompound(new NBTTagCompound());
        }
        drop.getTagCompound().setInteger("charge", this.getMetaFromState(state));
        spawnAsEntity(worldIn, pos, drop);
    }
}
