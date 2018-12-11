package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import com.sun.corba.se.impl.presentation.rmi.DynamicMethodMarshallerImpl;

import java.util.Vector;
import java.util.concurrent.Semaphore;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {
	private Vector<DeliveryVehicle> deliveryVehicles;
	private Semaphore sem;
	//thread safe singelton
	private static class SingletonHolderVehicle
	{
		private static ResourcesHolder instance = new ResourcesHolder();
	}
	private ResourcesHolder() {
		this.deliveryVehicles = new Vector<>();

			sem = new Semaphore(deliveryVehicles.size());
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static ResourcesHolder getInstance() {

		return ResourcesHolder.SingletonHolderVehicle.instance;
	}

	
	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	public Future<DeliveryVehicle> acquireVehicle() {
		try{
		sem.acquire();
		}
		catch (Exception e){}
	 Future <DeliveryVehicle> fu  = new Future<>();
	 fu.resolve(deliveryVehicles.remove(0));
	 return fu;
	}
	
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
		deliveryVehicles.add(vehicle);
		sem.release();
	}
	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		synchronized (deliveryVehicles) {
			for (int i = 0; i < vehicles.length; i = i + 1) {
				deliveryVehicles.add(vehicles[i]);
			}
		}
	}

}
