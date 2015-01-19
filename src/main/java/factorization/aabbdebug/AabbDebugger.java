package factorization.aabbdebug;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import factorization.shared.Core;
import factorization.shared.FzUtil;

public enum AabbDebugger {
    INSTANCE;
    
    private AabbDebugger() {
        Core.loadBus(this);
        ClientCommandHandler.instance.registerCommand(new ICommand() {
            public int compareTo(ICommand other) {
                return this.getCommandName().compareTo(other.getCommandName());
            }

            @Override
            public int compareTo(Object obj) {
                return this.compareTo((ICommand) obj);
            }

            @Override
            public String getCommandName() {
                return "boxdbg";
            }

            @Override
            public String getCommandUsage(ICommandSender p_71518_1_) {
                return "/bxdbg freeze|thaw";
            }

            @Override
            public List getCommandAliases() {
                return null;
            }

            @Override
            public void processCommand(ICommandSender player, String[] args) {
                if (args[0].equals("freeze")) {
                    freeze = true;
                } else if (args[0].equals("thaw")) {
                    frozen.clear();
                    frozen_lines.clear();
                }
            }

            @Override public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) { return true; }
            @Override public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) { return null; }
            @Override public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) { return false; }
            
        });
    }
    
    private static class Line {
        Vec3 start, end;
    }
    
    static ArrayList<AxisAlignedBB> boxes = new ArrayList(), frozen = new ArrayList();
    static ArrayList<Line> lines = new ArrayList(), frozen_lines = new ArrayList();
    static boolean freeze = false;
    
    public static void addBox(AxisAlignedBB box) {
        if (box == null) return;
        boxes.add(box.copy());
    }
    
    public static void addLine(Vec3 start, Vec3 end) {
        Line line = new Line();
        line.start = FzUtil.copy(start);
        line.end = FzUtil.copy(end);
        lines.add(line);
    }
    
    @SubscribeEvent
    public void clearBox(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            if (freeze) {
                if (!boxes.isEmpty() || !lines.isEmpty()) {
                    freeze = false;
                }
                frozen.addAll(boxes);
                frozen_lines.addAll(lines);
            }
            boxes.clear();
            lines.clear();
        }
    }
    
    boolean hasBoxes() {
        return !frozen.isEmpty() || !boxes.isEmpty() || !lines.isEmpty() || !frozen_lines.isEmpty();
    }
    
    @SubscribeEvent
    public void drawBoxes(RenderWorldLastEvent event) {
        if (!hasBoxes()) return;
        World w = Minecraft.getMinecraft().theWorld;
        if (w == null) return;
        EntityLivingBase camera = Minecraft.getMinecraft().renderViewEntity;
        double cx = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * (double) event.partialTicks;
        double cy = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * (double) event.partialTicks;
        double cz = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * (double) event.partialTicks;
        
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glPushMatrix();
        
        GL11.glTranslated(-cx, -cy, -cz);
        GL11.glDepthMask(false);
        //GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 0.5F);
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(1);
        for (AxisAlignedBB box : boxes) {
            RenderGlobal.drawOutlinedBoundingBox(box, 0x800000);
        }
        for (AxisAlignedBB box : frozen) {
            RenderGlobal.drawOutlinedBoundingBox(box, 0x4040B0);
        }
        GL11.glLineWidth(2);
        GL11.glColor4f(1, 1, 0, 1);
        GL11.glBegin(GL11.GL_LINES);
        for (Line line : lines) {
            GL11.glVertex3d(line.start.xCoord, line.start.yCoord, line.start.zCoord);
            GL11.glVertex3d(line.end.xCoord, line.end.yCoord, line.end.zCoord);
        }
        GL11.glEnd();
        GL11.glColor4f(0, 1, 1, 1);
        GL11.glBegin(GL11.GL_LINES);
        for (Line line : frozen_lines) {
            GL11.glVertex3d(line.start.xCoord, line.start.yCoord, line.start.zCoord);
            GL11.glVertex3d(line.end.xCoord, line.end.yCoord, line.end.zCoord);
        }
        GL11.glEnd();
        GL11.glDepthMask(true);
        
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
