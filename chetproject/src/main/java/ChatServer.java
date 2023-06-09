import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;


public class ChatServer {

    //서버소켓, 소켓 선언
    private ServerSocket server = null;
    private Socket socket = null;
    //소켓들을 담을 제네릭 타입의 ArrayList 참조변수, chatlist+ 객체생성
    ArrayList <ChatThread> chatlist = new ArrayList <ChatThread>();


    //프로그램 시작, 예외처리를 위한 try - catch 구문사용
    public void start() {
        try {
            //서버소켓 객체생성 서버포트번호를 매개변수로 받아서 생성과 동시에 소켓 바인딩
            server = new ServerSocket(4965);

            //서버시작시
            System.out.println("서버 시작");

            //client의 연결요청이 올때까지 반복하기위함
            while(true) {

                //accept()-> 연결요청 올때까지 블로킹, 요청이오면 통신용 socket 생성
                socket = server.accept();

                //쓰레드 객체 생성
                ChatThread chat = new ChatThread();

                //쓰레드 리스트 (chatlist)에 해당 쓰레드 추가
                chatlist.add(chat);

                //chat thread start
                chat.start();
            }
        } catch(Exception e) {

            //연결실패
            System.out.println("error");
        }
    }

    public static void main(String[] args) {

        ChatServer server = new ChatServer();
        server.start();  //서버시작
    }

   //메세지 전송
    void msgSendAll(String msg) {

        //사용자 모두 메세지가 보이도록
        for(ChatThread chat : chatlist) {
            //채팅데이터들을 매개변수로 받아서 쓰레드 리스트에 있는 모든 쓰레드에게 출력
            chat.outMsg.println(msg);
        }
    }
    class ChatThread extends Thread {


        //채팅 메세지 관련 변수
        String msg;
        String[] rmsg;

        //메세지 입*출력 스트림
        private BufferedReader inMsg = null;
        private PrintWriter outMsg = null;

        public void run() {

            boolean status = true;

            //챗 thread 시작중 알림
            System.out.println("진행중");
            try {

                /*
                -입*출력 스트림
                -BufferedReader로 입력된 데이터가 버퍼를 거쳐 전달이되면서 처리 효율성을 올림. (Scanner기능?같은역할)
                -InputStreamReader를 사용해 바이트,문자 변환 중개자 역할
                */
                inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                /*
                -짧게 코드를 쓰기위한 PrintWriter
                -소켓 생성
                 */
                outMsg = new PrintWriter(socket.getOutputStream(), true);

                //오류없이 잘 되고 있을 떄
                while(status) {


                    /*
                    -메세지 수신
                    -readLine() -> BufferedReader 데이터 불러옴
                    */
                    msg = inMsg.readLine();
                    rmsg = msg.split("/");         //"/" 기준으로 문자나눔


                    /*
                    "exit"라는 채팅 입력시
                    -기존 대화내역 Clear
                    -상대 상태여부를 서버에 전달 후 프로그램 status를 false로 바꿈
                     */
                    if(rmsg[1].equals("exit")) {
                        chatlist.remove(this);
                        msgSendAll("server/" + rmsg[0] + "님이 나갔습니다.");

                        status = false;
                    }


                    /*
                    "login" 일때
                    -접속했다고 전달해줌
                     */
                    else if(rmsg[1].equals("login")) {
                        msgSendAll("server/"+rmsg[0]+"님이 들어왔습니다.");
                    }

                    //메시지 이용(주고받기)
                    else {
                        msgSendAll(msg);
                    }
                }

                this.interrupt();        //쓰레드 종료를 위한 interrupt
                System.out.println(this.getName()+"end");
            } catch(IOException e) {
                chatlist.remove(this);          //채팅대화창 clear
                System.out.println("블루스크린뜨고 클라이언트 내역이 사라졌다. 어캐살려내지?ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ");
            }
        }
    }

}
