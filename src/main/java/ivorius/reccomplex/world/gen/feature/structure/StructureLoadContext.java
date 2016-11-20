/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 30.03.15.
 */
public class StructureLoadContext
{
    @Nonnull
    public final AxisAlignedTransform2D transform;
    @Nonnull
    public final StructureBoundingBox boundingBox;

    public final boolean generateAsSource;

    public StructureLoadContext(@Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, boolean generateAsSource)
    {
        this.transform = transform;
        this.boundingBox = boundingBox;
        this.generateAsSource = generateAsSource;
    }
}
