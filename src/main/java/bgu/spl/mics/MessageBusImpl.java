package bgu.spl.mics;

import bgu.spl.mics.Messages.Broadcast;
import bgu.spl.mics.Messages.FiftyPercentDiscount;
import bgu.spl.mics.Messages.Message;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private ConcurrentHashMap<MicroService,BlockingQueue<FiftyPercentDiscount.Event>>missionsToService;
	private ConcurrentHashMap<Class<? extends Message>,BlockingQueue<MicroService>>servisesToEvents;
	private ConcurrentHashMap<Class<? extends Message>,BlockingQueue<MicroService>>servisesToBrodcasts;
	private static MessageBusImpl instance = null;
	//thread safe singelton
	private static class SingletonHolder {
		private static MessageBusImpl
				instance = new MessageBusImpl();}
	private MessageBusImpl() {}
	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends FiftyPercentDiscount.Event<T>> type, MicroService m) {
		synchronized(servisesToEvents) { //TODO:check if needed
			if (servisesToEvents.contains(type)) { //if the event already as a servise then add this to the queueu
				servisesToEvents.get(type).add(m);
			} else //create a new queue to this events queueu
			{
				BlockingQueue<MicroService> queue = new LinkedBlockingQueue<>();
				queue.add(m);
				servisesToEvents.put(type, queue);
			}
		}
		}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized(servisesToBrodcasts) { //TODO:check if needed
			if (servisesToBrodcasts.contains(type)) { //if the event already as a servise then add this to the queueu
				servisesToBrodcasts.get(type).add(m);
			} else //create a new queue to this events queueu
			{
				BlockingQueue<MicroService> queue = new LinkedBlockingQueue<>();
				queue.add(m);
				servisesToBrodcasts.put(type, queue);
			}
		}
	}

	@Override
	public <T> void complete(FiftyPercentDiscount.Event<T> e, T result) {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}

	//sends it to the queue of the apropriate microservies
	@Override
	public <T> Future<T> sendEvent(FiftyPercentDiscount.Event<T> e) {

            Class eventType = e.getClass();
            MicroService execute = servisesToEvents.get(eventType).poll();
            missionsToService.get(execute).add(e);
            Future<T> fu = new Future<>();
            fu.notifyAll(); //or notifay one?
        return fu;


    }

	@Override
	public void register(MicroService m) { //TODO: should check if try to register twice?
        missionsToService.put(m,new LinkedBlockingQueue<>());

	}

	@Override
	public void unregister(MicroService m) {
        //TODO:check if needed
            if(missionsToService.contains(m)){
                //remove m from servises queue
                synchronized(missionsToService) {
                    missionsToService.remove(m);
                }
                //unassign M from all events
                synchronized (servisesToEvents){
                    Iterator<Map.Entry<Class<? extends Message>, BlockingQueue<MicroService>>> itr = servisesToEvents.entrySet().iterator();

                    while(itr.hasNext())
                    {
                        Map.Entry<Class<? extends Message>, BlockingQueue<MicroService>> entry = itr.next();
                        if(entry.getValue().contains(m))
                            entry.getValue().remove(m);
                        itr.remove();
                    }
                }
                //unassign M from all brodcasts
                synchronized (servisesToBrodcasts){
                    Iterator<Map.Entry<Class<? extends Message>, BlockingQueue<MicroService>>> itr = servisesToBrodcasts.entrySet().iterator();

                    while(itr.hasNext())
                    {
                        Map.Entry<Class<? extends Message>, BlockingQueue<MicroService>> entry = itr.next();
                        if(entry.getValue().contains(m))
                            entry.getValue().remove(m);
                        itr.remove();
                    }
                }
        }
    }

	@Override
	//return the next mission and wait for it
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	

}
