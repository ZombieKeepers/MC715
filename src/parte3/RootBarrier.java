package parte3;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class RootBarrier implements Watcher {

	ZooKeeper zk;
	String currentProcessNode;
	String lastData = "";
	String host;
	Integer mutex;
	Integer size;
	String barrier;
	
	/**
     * Barrier constructor
     *
     * @param address
     * @param root
     * @param size
	 * @throws IOException 
     */
    RootBarrier(String hostPort, String address, String barrier, int size) throws IOException {
		this.host = hostPort;
        zk = new ZooKeeper(hostPort, 3000, this);
        
    	this.barrier = barrier;
        this.size = size;

          // Create barrier node
        if (zk != null) {
            try {
                Stat s = zk.exists(barrier, false);
                if (s == null) {
                    zk.create(barrier, new byte[0], Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
            } catch (KeeperException e) {
                System.out
                        .println("Keeper exception when instantiating queue: "
                                + e.toString());
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception");
            }
        }
    }


    boolean barrierGuardian() throws KeeperException, InterruptedException{

    	while (true) {
    		synchronized (mutex) {
    			List<String> list = zk.getChildren(barrier, true);

    			if (list.size() < size) {
    				mutex.wait();
    			} else {
    				Collections.sort(list);
    				for(int i = 0; i < size; i++) {
    					zk.delete(barrier + "/" + list.get(i), -1);
    				}
    				//esperando para ser utilizado novamente
    				mutex.wait();
    			}
    		}
    	}
    	
    }


	@Override
	public void process(WatchedEvent event) {
		// TODO Auto-generated method stub
		
	}
	    
}
