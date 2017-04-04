package gollorum.signpost.gui;

import java.awt.Color;
import java.io.IOException;

import gollorum.signpost.blocks.PostPostTile;
import gollorum.signpost.management.ConfigHandler;
import gollorum.signpost.management.PostHandler;
import gollorum.signpost.network.NetworkHandler;
import gollorum.signpost.network.messages.SendPostBasesMessage;
import gollorum.signpost.util.BaseInfo;
import gollorum.signpost.util.DoubleBaseInfo;
import gollorum.signpost.util.MyBlockPos.Connection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.client.FMLClientHandler;

public class SignGuiPost extends GuiScreen {

	private GuiTextField base1InputBox;
	private GuiTextField base2InputBox;

	private String std1 = "";
	private int col1 = 0;
	private boolean go1;
	
	private String std2 = "";
	private int col2 = 0;
	private boolean go2;
	
	private PostPostTile tile;
	
	private boolean resetMouse;
	
	public SignGuiPost(PostPostTile tile) {
		this.tile = tile;
		base1InputBox = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 68, this.height / 2 - 46, 137, 20);
		base2InputBox = new GuiTextField(1, this.fontRendererObj, this.width / 2 - 68, this.height / 2 + 40, 137, 20);
		resetMouse = true;
	}

	@Override
	public void initGui() {
		super.initGui();
		DoubleBaseInfo tilebases = tile.getBases();
		base1InputBox = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 68, this.height / 2 - 46, 137, 20);
		base1InputBox.setMaxStringLength(23);
		base1InputBox.setText(tilebases.base1==null?"":tilebases.base1.toString());
		go1 = true;
		
		base2InputBox = new GuiTextField(1, this.fontRendererObj, this.width / 2 - 68, this.height / 2 + 40, 137, 20);
		base2InputBox.setMaxStringLength(23);
		base2InputBox.setText(tilebases.base2==null?"":tilebases.base2.toString());
		go2 = true;
		resetMouse = true;
	}
	
	@Override
    protected void mouseClicked(int x, int y, int bla) throws IOException{
		super.mouseClicked(x, y, bla);
		base1InputBox.mouseClicked(x, y, bla);
		base2InputBox.mouseClicked(x, y, bla);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if(mc==null){
			mc = FMLClientHandler.instance().getClient();
		}
		drawDefaultBackground();
		base1InputBox.drawTextBox();
		this.drawCenteredString(fontRendererObj, std2, this.width/2, base1InputBox.yPosition+25, col2);
		base2InputBox.drawTextBox();
		this.drawCenteredString(fontRendererObj, std1, this.width/2, base2InputBox.yPosition+25, col1);
		if(resetMouse){
			resetMouse = false;
			org.lwjgl.input.Mouse.setGrabbed(false);
		}
	}

	@Override
	protected void keyTyped(char par1, int par2) throws IOException {
		super.keyTyped(par1, par2);
		baseType(par1, par2, false);
		baseType(par1, par2, true);
	}
	
	private void baseType(char par1, int par2, boolean base2){
		GuiTextField tf = base2?base2InputBox:base1InputBox;
		String before = tf.getText();
		if(tf.textboxKeyTyped(par1, par2)&&!tf.getText().equals(before)){
			if(ConfigHandler.deactivateTeleportation){
				return;
			}
			BaseInfo inf = PostHandler.getWSbyName(tf.getText());
			Connection connect = tile.toPos().canConnectTo(inf);
			if(inf==null||!connect.equals(Connection.VALID)){
				tf.setTextColor(Color.red.getRGB());
				if(connect.equals(Connection.DIST)){
					String out = I18n.translateToLocal("signpost.guiTooFar");
					out = out.replaceAll("<distance>", ""+(int)tile.toPos().distance(inf.pos)+1);
					out = out.replaceAll("<maxDist>", ""+ConfigHandler.maxDist);
					if(base2){
						std1 = out;
						col1 = Color.red.getRGB();
						go1 = false;
					}else{
						std2 = out;
						col2 = Color.red.getRGB();
						go2 = false;
					}
					
				}else if(connect.equals(Connection.WORLD)){

					String out = I18n.translateToLocal("signpost.guiWoldDim");
					if(base2){
						std1 = out;
						col1 = Color.red.getRGB();
						go1 = false;
					}else{
						std2 = out;
						col2 = Color.red.getRGB();
						go2 = false;
					}
					
				}else{
					if(base2){
						std1 = "";
						col1 = Color.red.getRGB();
						go1 = false;
					}else{
						std2 = "";
						col2 = Color.red.getRGB();
						go2 = false;
					}
				}
			}else{
				tf.setTextColor(Color.white.getRGB());
				if(base2){
					col1 = Color.white.getRGB();
					go1 = true;
				}else{
					col2 = Color.white.getRGB();
					go2 = true;
				}

				if(!(ConfigHandler.deactivateTeleportation||ConfigHandler.cost==null)){
					String out = I18n.translateToLocal("signpost.guiPrev");
					int distance = (int) tile.toPos().distance(inf.pos)+1;
					out = out.replaceAll("<distance>", ""+distance);
					out = out.replaceAll("<amount>", Integer.toString((int) (tile.toPos().distance(inf.pos)/ConfigHandler.costMult+1)));
					out = out.replaceAll("<itemName>", ConfigHandler.costName());
					if(base2){
						col1 = Color.white.getRGB();
						std1 = out;
					}else{
						col2 = Color.white.getRGB();
						std2 = out;
					}
				}
			}
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		DoubleBaseInfo tilebases = tile.getBases();
		if(ConfigHandler.deactivateTeleportation||go2){
			tilebases.base1 = PostHandler.getWSbyName(base1InputBox.getText());
		}else{
			tilebases.base1 = null;
		}
		if(ConfigHandler.deactivateTeleportation||go1){
			tilebases.base2 = PostHandler.getWSbyName(base2InputBox.getText());
		}else{
			tilebases.base2 = null;
		}
		NetworkHandler.netWrap.sendToServer(new SendPostBasesMessage(tile, tilebases));
	}
}
