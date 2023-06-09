import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClientServer implements ActionListener, Runnable {
    private final String ip;
    private String id;
    private Socket socket;
    private BufferedReader inMsg = null;
    private PrintWriter outMsg = null;

    private final JButton loginButton;
    private final JTextField idInput;

    private final JLabel label2;
    private final JButton logoutButton;

    private final JTextField msgInput;
    private final JButton exitButton;

    private final JTextArea msgOut;

    private final Container tab;
    private final CardLayout clayout;
    private Thread thread;

    boolean status;

    public ClientServer(String ip) {   //생성자 ip주소를 받아서 기본적인 채팅창 구성
        this.ip = ip;                  //ip주소를 매개변수로 받아 멤버변수에 대입

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BorderLayout());
        idInput = new JTextField(15);
        loginButton = new JButton("로그인");
        loginButton.addActionListener(this);
        JLabel label1 = new JLabel("대화명");

        loginPanel.add(label1, BorderLayout.WEST);
        loginPanel.add(idInput, BorderLayout.CENTER);
        loginPanel.add(loginButton, BorderLayout.EAST);


        JPanel logoutPanel = new JPanel();

        logoutPanel.setLayout(new BorderLayout());
        label2 = new JLabel();
        logoutButton = new JButton("로그아웃");

        logoutButton.addActionListener(this);

        logoutPanel.add(label2, BorderLayout.CENTER);
        logoutPanel.add(logoutButton, BorderLayout.EAST);


        JPanel msgPanel = new JPanel();

        msgPanel.setLayout(new BorderLayout());
        msgInput = new JTextField(30);

        msgInput.addActionListener(this);
        exitButton = new JButton("종료");
        exitButton.addActionListener(this);

        msgPanel.add(msgInput, BorderLayout.CENTER);
        msgPanel.add(exitButton, BorderLayout.EAST);

        tab = new JPanel();
        clayout = new CardLayout();
        tab.setLayout(clayout);
        tab.add(loginPanel, "login");
        tab.add(logoutPanel, "logout");


        JFrame jframe = new JFrame("::멀티챗::");
        msgOut = new JTextArea("", 10, 30);

        msgOut.setEditable(false); //채팅창 수정불가

        JScrollPane jsp = new JScrollPane(msgOut, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jframe.add(tab, BorderLayout.NORTH);
        jframe.add(jsp, BorderLayout.CENTER);
        jframe.add(msgPanel, BorderLayout.SOUTH);

        clayout.show(tab, "login");

        jframe.pack();

        jframe.setResizable(false);

        jframe.setVisible(true);

    }

    public void connectServer() {
        try {
            //소켓객체생성 +연결(서버ip주소,서버포트번호)
            socket = new Socket(ip, 8888);
            System.out.println("[Client]Server 연결 성공!!");

            //socket.getInputStream() 통신소켓에 인풋스트림 객체를 얻음
            //new InputStreamReader(socket.getInputStream()->생성된 바이트기반 인풋스트림을 문자기반 스트림으로 연결
            // 인코딩 매개변수가 없으므로 해당 os에서 사용하는 기본 인코딩 문자로 변환하는 InputStreamReader를 생성
            //입출력의 효율을 높이기 위해 보조스트림(BufferedReader)연결
            inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //다양한 문자처리가 편한 PrintWriter 객체생성(auto flush값이 true이므로 println메서드 호출 될 때마다 자동으로 flush
            outMsg = new PrintWriter(socket.getOutputStream(), true);


            outMsg.println(id+"/"+"login"); //채팅창에 login을 친 효과


            thread = new Thread(this);
            thread.start();
        }/*catch(UnknownHostException e) {
        	System.out.println("해당 서버를 찾을 수 없습니다. IP주소를 확인하세요");
        }catch (IOException e) {
			System.out.println("해당 포트에 연결할 수 없습니다. 포트번호를 확인하세요");
		}*/catch(Exception e) {
            // e.printStackTrace();
            System.out.println("[MultiChatClient]connectServer() Exception 발생!!");
        }
    }


    public void actionPerformed(ActionEvent arg0) {
        Object obj = arg0.getSource();


        if(obj == exitButton) {//종료버튼 누르면 시스템 종료
            System.exit(0);
        } else if(obj == loginButton) {//로그인 버튼을 누를경우
            id = idInput.getText(); //id입력칸에 입력값을 text로 변환 변수 id에 대입

            label2.setText("대화명 : " + id);//창 레이블 변경
            clayout.show(tab, "logout"); //로그아웃 버튼으로 바꾸기
            connectServer(); //connectServer() 호출
        } else if(obj == logoutButton) { //로그아웃 버튼 클릭한 경우

            outMsg.println(id + "/" + "logout"); //채팅창에 logout을 입력하고 전송한것과 같은 효과

            msgOut.setText(""); //전에 입력한 내용을 전송하고 칸을 비워줌

            clayout.show(tab, "login"); //로그인버튼으로 바꾸기
            outMsg.close();//생성한 printwriter 닫기
            try {
                inMsg.close(); // berfferdReader 닫기
                socket.close(); //통신소켓 닫기
            } catch(IOException e) {
                e.printStackTrace();
            }

            status = false; //무한반복 종료
        } else if(obj == msgInput) { //단순 전송일경우

            outMsg.println(id + "/" + msgInput.getText());//id와 입력한 채팅 내용 텍스트로 변환 후"/"로 구분하여 출력

            msgInput.setText(""); //채팅내용 입력창 초기화
        }
    }

    public void run() { //쓰레드 작업 정의

        String msg; //채팅 내용을 담을 String 변수 선언
        String[] rmsg; // String 배열 변수 선언

        status = true; // 무한루프를 위해 while값 true로 변경

        while(status) {
            try {

                msg = inMsg.readLine();//한 줄을 통째로 읽어서 msg에 대입
                rmsg = msg.split("/");// 읽어들인 text를 "/"로 구분해서 각 인덱스에 대입


                msgOut.append(rmsg[0] + ">"+rmsg[1] + "\n"); //채팅 내용창에 추가 (인덱스 [0] 은 id [1]은 내용 + 줄 변경)


                msgOut.setCaretPosition(msgOut.getDocument().getLength());
            } catch(IOException e) {
                // e.printStackTrace();
                status = false; //예외가 발생하면 무한루프를 나옴
            }
        }

        System.out.println("[MultiChatClient]" + thread.getName() + "종료됨"); //콘솔에 해당쓰레드 종료 출력
    }

    public static void main(String[] args) {
        ClientServer mcc = new ClientServer("211.153.224.212");
    }
}
