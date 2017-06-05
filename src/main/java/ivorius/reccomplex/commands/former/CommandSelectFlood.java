/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.CommandExpecting;
import ivorius.reccomplex.commands.parameters.Expect;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.utils.RCBlockLogic;
import ivorius.reccomplex.utils.expression.PreloadedBooleanExpression;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectFlood extends CommandExpecting implements CommandVirtual
{
    public static final int MAX_FLOOD = 50 * 50 * 50;

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "flood";
    }

    @Override
    public Expect<?> expect()
    {
        return RCExpect.expectRC()
                .block()
                .metadata()
                .directionExpression();
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MockWorld world, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args, expect()::declare);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(commandSender, null, true);
        RCCommands.assertSize(commandSender, selectionOwner);

        PreloadedBooleanExpression<EnumFacing> facingExpression = PreloadedBooleanExpression.with(exp ->
        {
            exp.addConstants(EnumFacing.values());
            exp.addEvaluators(axis -> facing -> facing.getAxis() == axis, EnumFacing.Axis.values());
            exp.addEvaluator("horizontal", f -> f.getHorizontalIndex() >= 0);
            exp.addEvaluator("vertical", f -> f.getHorizontalIndex() < 0);
        });
        facingExpression.setExpression(parameters.get().move(2).text().optional().orElse(""));

        List<EnumFacing> available = Arrays.stream(EnumFacing.values()).filter(facingExpression).collect(Collectors.toList());

        List<BlockPos> dirty = Lists.newArrayList(selectionOwner.getSelection());
        Set<BlockPos> visited = Sets.newHashSet(dirty);

        Block dstBlock = parameters.mc().block(commandSender).require();
        int[] dstMeta = parameters.rc().move(1).metadatas().optional().orElse(new int[1]);
        List<IBlockState> dst = IntStream.of(dstMeta).mapToObj(m -> BlockStates.fromMetadata(dstBlock, m)).collect(Collectors.toList());

        while (!dirty.isEmpty())
        {
            BlockPos pos = dirty.remove(dirty.size() - 1);

            for (EnumFacing facing : available)
            {
                BlockPos offset = pos.offset(facing);
                if (RCBlockLogic.isAir(world, offset) && visited.add(offset))
                    dirty.add(offset);
            }

            if (visited.size() > MAX_FLOOD)
                throw new CommandException("Area too big to flood!");
        }

        for (BlockPos pos : visited)
        {
            IBlockState state = dst.get(world.rand().nextInt(dst.size()));
            world.setBlockState(pos, state, 2);
        }
    }
}
