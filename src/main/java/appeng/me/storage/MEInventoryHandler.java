/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.me.storage;


import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.prioitylist.DefaultPriorityList;
import appeng.util.prioitylist.IPartitionList;


public class MEInventoryHandler<T extends IAEStack<T>> implements IMEInventoryHandler<T>
{

	final StorageChannel channel;
	final protected IMEMonitor<T> monitor;
	final protected IMEInventoryHandler<T> internal;

	private int myPriority;
	private IncludeExclude myWhitelist;
	private AccessRestriction myAccess;
	private IPartitionList<T> myPartitionList;

	private AccessRestriction cachedAccessRestriction;
	private boolean hasReadAccess;
	private boolean hasWriteAccess;

	public MEInventoryHandler( IMEInventory<T> i, StorageChannel channel )
	{
		this.channel = channel;

		if ( i instanceof IMEInventoryHandler )
			this.internal = ( IMEInventoryHandler<T> ) i;
		else
			this.internal = new MEPassThrough<T>( i, channel );

		this.monitor = this.internal instanceof IMEMonitor ? ( IMEMonitor<T> ) this.internal : null;

		this.setPriority( 0 );
		this.setWhitelist( IncludeExclude.WHITELIST );
		this.setBaseAccess( AccessRestriction.READ_WRITE );
		this.setPartitionList( new DefaultPriorityList<T>() );
	}

	@Override
	public int getPriority()
	{
		return this.myPriority;
	}

	public void setPriority( int myPriority )
	{
		this.myPriority = myPriority;
	}

	public IncludeExclude getWhitelist()
	{
		return this.myWhitelist;
	}

	public void setWhitelist( IncludeExclude myWhitelist )
	{
		this.myWhitelist = myWhitelist;
	}

	public AccessRestriction getBaseAccess()
	{
		return this.myAccess;
	}

	public void setBaseAccess( AccessRestriction myAccess )
	{
		this.myAccess = myAccess;
		this.cachedAccessRestriction = this.myAccess.restrictPermissions( this.internal.getAccess() );
		this.hasReadAccess = this.getAccess().hasPermission( AccessRestriction.READ );
		this.hasWriteAccess = this.getAccess().hasPermission( AccessRestriction.WRITE );
	}

	public IPartitionList<T> getPartitionList()
	{
		return this.myPartitionList;
	}

	public void setPartitionList( IPartitionList<T> myPartitionList )
	{
		this.myPartitionList = myPartitionList;
	}

	@Override
	public T injectItems( T input, Actionable type, BaseActionSource src )
	{
		if ( !this.canAccept( input ) )
			return input;

		return this.internal.injectItems( input, type, src );
	}

	@Override
	public T extractItems( T request, Actionable type, BaseActionSource src )
	{
		if ( !hasReadAccess )
			return null;

		return this.internal.extractItems( request, type, src );
	}

	@Override
	public IItemList<T> getAvailableItems( IItemList<T> out )
	{
		if ( !hasReadAccess )
			return out;

		return this.internal.getAvailableItems( out );
	}

	@Override
	public StorageChannel getChannel()
	{
		return this.internal.getChannel();
	}

	@Override
	public AccessRestriction getAccess()
	{
		return this.cachedAccessRestriction;
	}

	@Override
	public boolean isPrioritized( T input )
	{
		if ( this.myWhitelist == IncludeExclude.WHITELIST )
			return this.myPartitionList.isListed( input ) || this.internal.isPrioritized( input );
		return false;
	}

	@Override
	public boolean canAccept( T input )
	{
		if ( !hasWriteAccess )
			return false;

		if ( this.myWhitelist == IncludeExclude.BLACKLIST && this.myPartitionList.isListed( input ) )
			return false;
		if ( this.myPartitionList.isEmpty() || this.myWhitelist == IncludeExclude.BLACKLIST )
			return this.internal.canAccept( input );
		return this.myPartitionList.isListed( input ) && this.internal.canAccept( input );
	}

	@Override
	public int getSlot()
	{
		return this.internal.getSlot();
	}

	public IMEInventory<T> getInternal()
	{
		return this.internal;
	}

	@Override
	public boolean validForPass( int i )
	{
		return true;
	}

}
