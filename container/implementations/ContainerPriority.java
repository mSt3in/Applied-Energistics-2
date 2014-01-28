package appeng.container.implementations;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.tileentity.TileEntity;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.helpers.IPriorityHost;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerPriority extends AEBaseContainer
{

	IPriorityHost priHost;

	@SideOnly(Side.CLIENT)
	public GuiTextField textField;

	@SideOnly(Side.CLIENT)
	public void setTextField(GuiTextField level)
	{
		textField = level;
		textField.setText( "" + PriorityValue );
	}

	public ContainerPriority(InventoryPlayer ip, IPriorityHost te) {
		super( ip, (TileEntity) (te instanceof TileEntity ? te : null), (IPart) (te instanceof IPart ? te : null) );
		priHost = te;
	}

	int PriorityValue = -1;

	public void setPriority(int newValue, EntityPlayer player)
	{
		priHost.setPriority( newValue );
		PriorityValue = newValue;
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			for (int i = 0; i < this.crafters.size(); ++i)
			{
				ICrafting icrafting = (ICrafting) this.crafters.get( i );

				if ( this.PriorityValue != priHost.getPriority() )
				{
					icrafting.sendProgressBarUpdate( this, 2, (int) priHost.getPriority() );
				}
			}

			this.PriorityValue = (int) priHost.getPriority();
		}
	}

	@Override
	public void updateProgressBar(int idx, int value)
	{
		super.updateProgressBar( idx, value );

		if ( idx == 2 )
		{
			PriorityValue = value;
			if ( textField != null )
				textField.setText( "" + PriorityValue );
		}
	}

}