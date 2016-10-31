/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.utils.expression.PositionedBlockMatcher;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerProperty;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSetProperty extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "property";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.selectProperty.usage");
    }

    @Nonnull
    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, TransformerProperty.propertyNameStream().collect(Collectors.toSet()));
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, TransformerProperty.propertyValueStream(args[0]).collect(Collectors.toSet()));
        else if (args.length == 3)
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());

        return Collections.emptyList();
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length >= 2)
        {
            World world = commandSender.getEntityWorld();

            PositionedBlockMatcher matcher = new PositionedBlockMatcher(RecurrentComplex.specialRegistry, args.length > 2 ? buildString(args, 2) : "");

            String propertyName = args[0];
            String propertyValue = args[1];

            for (BlockPos pos : RCBlockAreas.mutablePositions(RCCommands.getSelectionOwner(commandSender, null, true).getSelection()))
            {
                PositionedBlockMatcher.Argument at = PositionedBlockMatcher.Argument.at(world, pos);
                if (matcher.test(at))
                    TransformerProperty.withProperty(at.state, propertyName, propertyValue).ifPresent(state -> world.setBlockState(pos, state, 3));
            }
        }
        else
        {
            throw ServerTranslations.wrongUsageException("commands.selectProperty.usage");
        }
    }
}