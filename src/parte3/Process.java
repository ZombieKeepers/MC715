package parte3;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class Process implements Watcher{
	
	ZooKeeper zk;
	String myNode;
	String barrier;
	
	public Process(String hostPort, String barrier) {
		this.barrier = barrier;
		try {
			zk = new ZooKeeper(hostPort, 3000, this);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
    void enter() throws KeeperException, InterruptedException{
    	System.out.println("Ponto de sincronização...\nEntrando na barreira e esperando para sair... ");
    
    	this.myNode = zk.create(this.barrier + "/" , new byte[0], Ids.OPEN_ACL_UNSAFE,
    			CreateMode.EPHEMERAL_SEQUENTIAL);
    	zk.exists(this.myNode, true);
      
    	//Fica ouvindo esperando ser liberado da barreira
    	while (true) {
    		synchronized (this) {
    			wait();
    		}
    	}
    	
  }

    //Quando receber que nó morreu, ativa a função leave()
	@Override
	public void process(WatchedEvent event) {
		switch(event.getType()) {
	    //Quando receber que nó morreu, ativa a função leave()
		case NodeDeleted:
			System.out.println("Fui liberado da barreira!");
			leave();
			break;
		default:
			break;
		}
		
	}

	private void leave() {
		System.out.println("Sai da barreira, posso continuar meu processamento!! =) ");
	}
	
	public static void main(String[] args) {
		System.out.println("Inciando processo " + Thread.currentThread().getId());
        String hostPort = args[0];
        String znode = args[1];
        try {
            new Process(hostPort, znode).enter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 		
}
