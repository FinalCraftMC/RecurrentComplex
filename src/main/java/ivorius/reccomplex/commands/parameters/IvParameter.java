/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.parameters;

import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 31.05.17.
 */
public class IvParameter extends Parameter
{
    public IvParameter(Parameter other)
    {
        super(other);
    }

    // Since CommandBase's version requires a sender
    public static BlockSurfacePos parseSurfacePos(BlockPos blockpos, String x, String z, boolean centerBlock) throws NumberInvalidException
    {
        return BlockSurfacePos.from(new BlockPos(CommandBase.parseDouble((double) blockpos.getX(), x, -30000000, 30000000, centerBlock), 0, CommandBase.parseDouble((double) blockpos.getZ(), z, -30000000, 30000000, centerBlock)));
    }

    @Override
    public IvParameter move(int idx)
    {
        return new IvParameter(super.move(idx));
    }

    @Nonnull
    public Parameter.Result<BlockSurfacePos> surfacePos(BlockPos ref, boolean centerBlock)
    {
        return first().missable().flatMap(x -> at(1).map(z ->
                parseSurfacePos(ref, x, z, centerBlock)))
                .orElse(() -> BlockSurfacePos.from(ref));
    }

    public Result<AxisAlignedTransform2D> transform(boolean mirror) throws CommandException
    {
        if (has(1) || mirror)
            return first().missable().map(CommandBase::parseInt).orElse(() -> 0).map(r -> AxisAlignedTransform2D.from(r, mirror));
        return Result.empty();
    }
}