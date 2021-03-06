/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBiomeMatcher;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 05.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceBiomeGen extends TableDataSourceSegmented
{
    private WeightedBiomeMatcher generationInfo;

    private TableDelegate tableDelegate;

    public TableDataSourceBiomeGen(WeightedBiomeMatcher generationInfo, TableDelegate tableDelegate)
    {
        this.generationInfo = generationInfo;
        this.tableDelegate = tableDelegate;

        addSegment(0, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.biomes"), generationInfo.getBiomeExpression(), null));

        addSegment(1, () -> {
            return RCGuiTables.defaultWeightElement(val -> generationInfo.setGenerationWeight(TableCells.toDouble(val)), generationInfo.getGenerationWeight());
        });
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Biome";
    }
}
