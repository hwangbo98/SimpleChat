//https://github.com/hwangbo98/SimpleChat.git 21800801 황보연 
import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(4000);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	/*
	 * run의 함수 안에서는 현재 '/userlist'를 치게 되면 send_userlist()가 실행됨
	 * 그리고 내가 설정해 놓은 욕들이 들어간 문장이 보이게 되면 경고문을 띄우고, 그 msg
	 * 전송하지 않는다.
	 */
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}
				else if(line.equals("/userlist")){
					send_userlist();
				}
				else if(line.contains("shit")) {
					send_badword();
				}
				else if(line.contains("바보")) {
					send_badword();
				}
				else if(line.contains("멍청이")) {
					send_badword();
				}
				else if(line.contains("idiot")) {
					send_badword();
				}
				else if(line.contains("fuck")) {
					send_badword();
				}
				else 
				broadcast(id + " : " + line);
				 
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	/*
	 * broadcast의 경우에는 Collection을 통해 hm.values에 있는 걸 가져옴
	 * 그리고 나서 우리가 PrintWriter를 통해서 각각의 id에서 입력한 것을 전달하는 것이므로
	 * 현재 id의 value를 pw2에 저장한 후에 반복문으로 value값에 있는거를 pw에 저장
	 * 그리고 그 저장된 pw와 pw2와 value가 일치하지 않으면 이 pw에다가 msg를 출력하지만
	 * 일치하면 아무것도 하지 않는다,(즉, 자신에게 msg가 나타나지 않음)  */
	public void broadcast(String msg){ // 일반 메세지를 만든다.
		synchronized(hm){
			Collection collection = hm.values(); //콜렉션을 통해 hm.values에 있는 모든 밸류를 가져옴.
			Iterator iter = collection.iterator();
			PrintWriter pw2 = (PrintWriter)hm.get(id);
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if(pw!=pw2) {
				pw.println(msg);// 메세지를 모두에게 보냄.
				pw.flush();
				}
				else ;
			}
		}
	} // broadcast
	/*
	 * send_userlist의 경우엔 위의 broadcast의 경우와 거의 비슷하다.
	 * keySet을 통해서 hashmap의 key들을 모두 가져온다. 그리고 현재 id의 value값을 pw1에 저장
	 * 그리고 나서, iter1이 아무것도 없을 때까지 반복문을 통해서, key값들을 key라는 string 변수에 저장한다.
	 * 그리고 현재 id 값의 value값이 pw1에다가 접속한 이름을 출력을 한다.
	 * 그리고 while문 종료 후, 총 참여자 수를 구할때는 hashmap의 size를 출력한다.*/
	public void send_userlist() { //broadcast와 거의 비슷함. key값을 가져온다. 그리고 자기 자신에게 보낸다.
		synchronized(hm) {
			Iterator iter1 = hm.keySet().iterator();
			PrintWriter pw1 = (PrintWriter)hm.get(id);
			String key;
			while(iter1.hasNext()) {
				key = (String)iter1.next();
				pw1.println("User :" + key);
				pw1.flush();
			}
			pw1.println("총 참여 수:" +hm.size());
			pw1.flush();
			
		}
	}
	/*
	 * send_badword의 경우에도 역시  broadcast와 비슷하게 하면 됨
	 * values값들을 iter2에 저장한다. 그리고 pw3에다가 현재 id의 value값을 저장함
	 * iter2의 value들을 pw에 저장하고, 만약에 pw3과 pw의 value가 같다 즉, 같은 사람이면,
	 * 이제 pw의 값에다가 욕하지말라 경고문을 띄운다. */
	public void send_badword() {
		synchronized(hm) {
			Iterator iter2 = hm.values().iterator();
			PrintWriter pw3 = (PrintWriter)hm.get(id);
			while(iter2.hasNext()){
				PrintWriter pw = (PrintWriter)iter2.next();
				if(pw==pw3) {
					pw.println("Caution! Don't say that!");
					pw.flush();
				}
			}
		}
	}
}