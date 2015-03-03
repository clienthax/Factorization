package factorization.mechanisms;

import factorization.common.FactoryType;
import factorization.shared.BlockRenderHelper;
import factorization.shared.FactorizationBlockRender;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;

public class BlockRenderHinge extends FactorizationBlockRender {
    @Override
    public boolean render(RenderBlocks rb) {
        BlockRenderHelper block = BlockRenderHelper.instance;
        block.useTexture(Blocks.iron_block.getIcon(0, 0));
        if (world_mode) {
            TileEntityHinge hinge = (TileEntityHinge) te;
            hinge.setBlockBounds(block);
            block.render(rb, getCoord());
        } else {
            getFactoryType().getRepresentative().setBlockBounds(block);
            block.renderForInventory(rb);
        }
        return true;
    }

    @Override
    public FactoryType getFactoryType() {
        return FactoryType.HINGE;
    }
}
