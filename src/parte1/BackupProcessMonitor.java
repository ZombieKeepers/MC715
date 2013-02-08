package parte1;
import java.io.IOException;


import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * Processo que monitora um znode. 
 * Se znode morrer invoca processo backup. 
 * @author ra092109
 *
 */

public class BackupProcessMonitor implements Watcher {

	boolean currentProcessDead = false;

    ZooKeeper zk;
    String currentProcessNode;
    String lastData = "";
    String host;
    
    

	public BackupProcessMonitor(String hostPort, String znode) throws KeeperException, IOException {
		this.host = hostPort;
        zk = new ZooKeeper(hostPort, 3000, this);
        this.currentProcessNode = znode;

    }

	public void watch() {
		zk.exists(this.currentProcessNode, true, null, null);
		
		//Enquanto processo principal está vivo não há nada a ser feito.
		try {
			synchronized (this) { 
				while (!currentProcessDead) {
					wait();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void createNewProcess() {
		System.out.println("Criando novo processo");
		try {
			new CurrentProcess(host, "/BACKUP", lastData).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.exit(0);
	}
	
	@Override
	public void process(WatchedEvent event) {
		switch(event.getType()) {
		case NodeDataChanged:			
			try {
				String data =  new String(zk.getData(currentProcessNode, true, null),  "UTF-8");
				System.out.println("Novo dado: " + data);
				this.lastData = data;

				if(data.trim().equals("morra"))
					zk.delete(this.currentProcessNode, -1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;
		case NodeCreated:
			System.out.println("Nó criado " + event.getPath());
			break;
		case NodeChildrenChanged:
			System.out.println("Child mudou " + event.getPath());
			break;
		case NodeDeleted:
			currentProcessDead = true;
			System.out.println("Deletado");
			createNewProcess();
			break;
		case None:
			switch(event.getState()) {
			case Expired:
				System.out.println("expired");
				break;
			case Disconnected:
				System.out.println("disconnected");
				break;
			}
			break;
		}
	}
	
	public static void main(String[] args) {
		System.out.println("Inciando watcher do proceso...");
        String hostPort = args[0];
        String znode = args[1];
        try {
            new BackupProcessMonitor(hostPort, znode).watch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
