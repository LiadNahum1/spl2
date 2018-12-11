package bgu.spl.mics.application.services;

import bgu.spl.mics.Messages.AcquireVehicleEvent;
import bgu.spl.mics.Messages.ReleaseVehicleEvent;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService{
	private ResourcesHolder holder;
	public ResourceService() {
		super("Change_This_Name");
		holder = ResourcesHolder.getInstance();
	}

	@Override
	protected void initialize() {
		subscribeEvent(AcquireVehicleEvent.class ,event ->{
			holder.acquireVehicle();
		} );

		subscribeEvent(ReleaseVehicleEvent.class , event ->{
			holder.releaseVehicle(event.getVehicle());
		} );
	}

}
