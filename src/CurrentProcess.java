import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;



public class CurrentProcess {

    ZooKeeper zk;
    
    String znode;


	public CurrentProcess(String hostPort, String znode, String nodeContent) throws KeeperException, IOException {
        zk = new ZooKeeper(hostPort, 3000, null);
        this.znode = znode;
        try {
			zk.create(znode, nodeContent.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }


	public void run() throws IOException, KeeperException, InterruptedException {
	
		InputStreamReader ir = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(ir);  
		
		while(!in.ready());
	
		String entrada = in.readLine();
		System.out.println("Digite um novo dado para setar:");
		
		while(!entrada.trim().equals("quit")) {
			System.out.println("Digite um novo dado para setar:");
			if(entrada != null) {
				zk.setData(znode, entrada.getBytes(), -1);
			}
			entrada = in.readLine();
		}
		
		in.close();
		ir.close();
		try {
			zk.delete(znode, -1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public static void main(String[] args) {
		System.out.println("Iniciando processo principal...");
        String hostPort = args[0];
        String znode = args[1];
        String content = args[2];
        try {
            new  CurrentProcess(hostPort, znode, content).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
