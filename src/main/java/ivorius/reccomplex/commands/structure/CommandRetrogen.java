/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.structure;

import ivorius.ivtoolkit.util.IvStreams;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.commands.parameters.RCExpect;
import ivorius.reccomplex.commands.parameters.RCParameters;
import ivorius.reccomplex.files.RCFiles;
import ivorius.reccomplex.files.loading.FileSuffixFilter;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.world.gen.feature.WorldGenStructures;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.RegionFileCache;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandRetrogen extends CommandBase
{
    public static Stream<Pair<Integer, Integer>> existingRegions(File worldDir)
    {
        File regionsDirectory = RCFiles.getValidatedFolder(new File(worldDir, "region"), false);
        if (regionsDirectory == null) return Stream.empty();

        String[] mcas = regionsDirectory.list(new FileSuffixFilter("mca"));
        if (mcas == null) throw new IllegalStateException();

        return Arrays.stream(mcas).map(s -> s.split("\\."))
                .filter(p -> p.length == 4 && p[0].equals("r")) // Is region file
                .map(p -> Pair.of(Integer.parseInt(p[1]), Integer.parseInt(p[2])))
                .filter(rfc -> rfc.getLeft() != null && rfc.getRight() != null); // Has coords
    }

    public static Stream<ChunkPos> existingChunks(World world)
    {
        // Each region is 32x32 chunks
        File worldDirectory = world.getSaveHandler().getWorldDirectory();
        return existingRegions(worldDirectory)
                .map(rfc -> new ChunkPos(rfc.getLeft() << 5, rfc.getRight() << 5))
                .map(rflc -> Pair.of(RegionFileCache.createOrLoadRegionFile(worldDirectory, rflc.x, rflc.z), rflc))
                .flatMap(r -> IvStreams.flatMapToObj(IntStream.range(0, 32), x -> IntStream.range(0, 32).mapToObj(z -> Pair.of(r.getLeft(), add(r.getRight(), x, z)))))
                .filter(p -> p.getLeft().chunkExists(p.getRight().x & 31, p.getRight().z & 31)) // Region has chunk
                .map(Pair::getRight);
    }

    @Nonnull
    protected static ChunkPos add(ChunkPos pos, int x, int z)
    {
        return new ChunkPos(x + pos.x, z + pos.z);
    }

    public static Random getRandom(WorldServer world, ChunkPos pos)
    {
        return world.setRandomSeed(pos.x, pos.z, 0xDEADBEEF);
    }

    public static long retrogen(WorldServer world, Predicate<Structure> structurePredicate)
    {
        return existingChunks(world)
                .filter(pos -> world.getChunkFromChunkCoords(pos.x, pos.z).isTerrainPopulated())
                .filter(pos -> WorldGenStructures.decorate(world, getRandom(world, pos), pos, structurePredicate))
                .count();
    }

    @Override
    public String getName()
    {
        return RCConfig.commandPrefix + "retro";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.rcretro.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        RCParameters parameters = RCParameters.of(args);

        Predicate<Structure> structurePredicate = parameters.rc("exp").structurePredicate().optional().orElse(null);
        WorldServer world = parameters.mc("dimension").dimension(server, commandSender).require();

        long count = retrogen(world, structurePredicate);

        commandSender.sendMessage(ServerTranslations.format("commands.rcretro.count", String.valueOf(count)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return RCExpect.expectRC()
                .named("exp").structurePredicate()
                .named("dimension").dimension()
                .get(server, sender, args, pos);
    }

}