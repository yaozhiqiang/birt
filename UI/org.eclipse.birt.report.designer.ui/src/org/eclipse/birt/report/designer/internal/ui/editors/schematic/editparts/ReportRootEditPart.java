/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Actuate Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts;

import org.eclipse.birt.report.designer.internal.ui.editors.notification.DeferredRefreshManager;
import org.eclipse.birt.report.designer.internal.ui.editors.parts.DeferredGraphicalViewer;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.tools.RootDragTracker;
import org.eclipse.birt.report.designer.internal.ui.layout.ReportDesignLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.AutoexposeHelper;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.FreeformGraphicalRootEditPart;
import org.eclipse.gef.editparts.GuideLayer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ViewportAutoexposeHelper;

/**
 * Root editPart
 *  
 */
public class ReportRootEditPart extends ScalableFreeformRootEditPart
{

	private DeferredRefreshManager refreshManager;

	/**
	 * Constructor
	 * 
	 * @param manager
	 */
	public ReportRootEditPart( DeferredRefreshManager manager )
	{
		this.refreshManager = manager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPart#getDragTracker(org.eclipse.gef.Request)
	 */
	public DragTracker getDragTracker( Request req )
	{
		return new RootDragTracker( );
	}

	/**
	 * gets the DeferredRefreshManager, all editPart have only one.
	 * 
	 * @return
	 */
	public DeferredRefreshManager getRefreshManager( )
	{
		return refreshManager;
	}

	/**
	 * Creates a layered pane and the layers that should be printed.
	 * 
	 * @see org.eclipse.gef.print.PrintGraphicalViewerOperation
	 * @return a new LayeredPane containing the printable layers
	 */
	protected LayeredPane createPrintableLayers( )
	{
		FreeformLayeredPane layeredPane = new FreeformLayeredPane( )
		{

			protected void paintFigure( Graphics graphics )
			{
				graphics.setBackgroundColor( ColorConstants.gray );
				graphics.fillRectangle( getBounds( ) );
			}
		};

		FreeformLayer layer = new FreeformLayer( )
		{

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.draw2d.FreeformLayer#getFreeformExtent()
			 */
			public Rectangle getFreeformExtent( )
			{
				Rectangle rect = super.getFreeformExtent( );
				Rectangle retValue = rect.getCopy( );
				Object obj = getViewer( ).getProperty(
						DeferredGraphicalViewer.REPORT_SIZE );
				if ( obj instanceof Rectangle )
				{
					Rectangle temp = (Rectangle) obj;
					if ( temp.width - rect.right( ) <= ReportDesignLayout.MINRIGHTSPACE )
					{
						retValue.width = retValue.width
								+ ReportDesignLayout.MINRIGHTSPACE;
					}
					if ( temp.height - rect.bottom( ) <= ReportDesignLayout.MINBOTTOMSPACE )
					{
						retValue.height = retValue.height
								+ ReportDesignLayout.MINBOTTOMSPACE;
					}

				}
				return retValue;
			}
		};
		layeredPane.add( layer, PRIMARY_LAYER );

		layeredPane.add( new ConnectionLayer( ), CONNECTION_LAYER );
		return layeredPane;
	}

	/**
	 * @see FreeformGraphicalRootEditPart#createLayers(LayeredPane)
	 */
	protected void createLayers( LayeredPane layeredPane )
	{
		layeredPane.add( getScaledLayers( ), SCALABLE_LAYERS );

		layeredPane.add( new FreeformLayer( ), HANDLE_LAYER );
		layeredPane.add( new FeedbackLayer( ), FEEDBACK_LAYER );
		layeredPane.add( new GuideLayer( ), GUIDE_LAYER );
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter == AutoexposeHelper.class )
			return new ReportViewportAutoexposeHelper( this );
		return super.getAdapter( adapter );
	}

	private static class ReportViewportAutoexposeHelper
			extends
				ViewportAutoexposeHelper
	{

		/** defines the range where autoscroll is active inside a viewer */
		private static final Insets DEFAULT_EXPOSE_THRESHOLD = new Insets( 18 );

		/** the last time an auto expose was performed */
		private long lastStepTime = 0;

		/** The insets for this helper. */
		private Insets threshold;

		/**
		 * Constructs a new helper on the given GraphicalEditPart. The editpart
		 * must have a <code>Viewport</code> somewhere between its
		 * <i>contentsPane </i> and its <i>figure </i> inclusively.
		 * 
		 * @param owner
		 *            the GraphicalEditPart that owns the Viewport
		 */
		public ReportViewportAutoexposeHelper( GraphicalEditPart owner )
		{
			super( owner );
			threshold = DEFAULT_EXPOSE_THRESHOLD;
		}

		/**
		 * Constructs a new helper on the given GraphicalEditPart. The editpart
		 * must have a <code>Viewport</code> somewhere between its
		 * <i>contentsPane </i> and its <i>figure </i> inclusively.
		 * 
		 * @param owner
		 *            the GraphicalEditPart that owns the Viewport
		 * @param threshold
		 *            the Expose Threshold to use when determing whether or not
		 *            a scroll should occur.
		 */
		public ReportViewportAutoexposeHelper( GraphicalEditPart owner,
				Insets threshold )
		{
			super( owner );
			this.threshold = threshold;
		}

		/**
		 * Returns <code>true</code> if the given point is inside the
		 * viewport, but near its edge.
		 * 
		 * @see org.eclipse.gef.AutoexposeHelper#detect(org.eclipse.draw2d.geometry.Point)
		 */
		public boolean detect( Point where )
		{
			lastStepTime = 0;
			Viewport port = findViewport( owner );
			Rectangle rect = Rectangle.SINGLETON;
			port.getClientArea( rect );
			port.translateToParent( rect );
			port.translateToAbsolute( rect );
			return rect.contains( where )
					&& !rect.crop( threshold ).contains( where );
		}

		/**
		 * Returns <code>true</code> if the given point is outside the
		 * viewport or near its edge. Scrolls the viewport by a calculated (time
		 * based) amount in the current direction.
		 * 
		 * todo: investigate if we should allow auto expose when the pointer is
		 * outside the viewport
		 * 
		 * @see org.eclipse.gef.AutoexposeHelper#step(org.eclipse.draw2d.geometry.Point)
		 */
		public boolean step( Point where )
		{
			Viewport port = findViewport( owner );

			Rectangle rect = Rectangle.SINGLETON;
			port.getClientArea( rect );
			port.translateToParent( rect );
			port.translateToAbsolute( rect );
			if ( !rect.contains( where )
					|| rect.crop( threshold ).contains( where ) )
				return false;

			// set scroll offset (speed factor)
			int scrollOffset = 0;

			// calculate time based scroll offset
			if ( lastStepTime == 0 )
				lastStepTime = System.currentTimeMillis( );

			DeferredGraphicalViewer.OriginStepData stepData = ( (DeferredGraphicalViewer) owner
					.getViewer( ) ).getOriginStepData( );
			long difference = System.currentTimeMillis( ) - lastStepTime;

			if ( difference > 0 )
			{
				scrollOffset = ( (int) difference / 3 );
				lastStepTime = System.currentTimeMillis( );
			}

			if ( scrollOffset == 0 )
				return true;

			rect.crop( threshold );

			int region = rect.getPosition( where );
			Point loc = port.getViewLocation( );

			if ( ( region & PositionConstants.SOUTH ) != 0 )
				loc.y += scrollOffset;
			else if ( ( region & PositionConstants.NORTH ) != 0 )
				loc.y -= scrollOffset;

			if ( ( region & PositionConstants.EAST ) != 0 )
				loc.x += scrollOffset;
			else if ( ( region & PositionConstants.WEST ) != 0 )
				loc.x -= scrollOffset;

			//System.out.println( "where ==" + where.x);
			Rectangle bounds = port.getBounds( );
			if ( stepData.minX > loc.x )
				loc.x = port.getHorizontalRangeModel( ).getValue( );
			if ( stepData.maxX - stepData.extendX < loc.x )
				loc.x = port.getHorizontalRangeModel( ).getValue( );
			if ( stepData.minY > loc.y )
				loc.y = port.getVerticalRangeModel( ).getValue( );
			if ( stepData.maxY - stepData.extendY < loc.y )
				loc.y = port.getVerticalRangeModel( ).getValue( );
			port.setViewLocation( loc );
			//initStepDat(port);
			return true;
		}

	}

	class FeedbackLayer extends FreeformLayer
	{

		FeedbackLayer( )
		{
			setEnabled( false );
		}
	}
}