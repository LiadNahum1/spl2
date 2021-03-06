package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.Messages.AcquireVehicleEvent;
import bgu.spl.mics.Messages.ReleaseVehicleEvent;
import bgu.spl.mics.Messages.TerminateBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService {
    private ResourcesHolder holder;
    private CountDownLatch countDownLatch;
    private Vector<Future<DeliveryVehicle>> unresolvedFut;

    public ResourceService(int num, CountDownLatch countDownLatch) {
        super("ResourceService" + num);
        this.countDownLatch = countDownLatch;
        holder = ResourcesHolder.getInstance();
        unresolvedFut = new Vector<>();
    }

    @Override
    protected void initialize() {
        subscribeEvent(AcquireVehicleEvent.class, event -> {
            Future<DeliveryVehicle> fu = holder.acquireVehicle();
            if (!fu.isDone()) {
                unresolvedFut.add(fu);
            }
            complete(event, fu);
        });

        subscribeEvent(ReleaseVehicleEvent.class, event -> {
            System.out.println("release" + getName());
            holder.releaseVehicle(event.getVehicle());
            complete(event, null);
        });
        subscribeBroadcast(TerminateBroadcast.class, broadcast -> {
            for (Future<DeliveryVehicle> vehicle : unresolvedFut) {
                vehicle.resolve(null);
            }
            terminate();
        });
        countDownLatch.countDown();
    }

}
