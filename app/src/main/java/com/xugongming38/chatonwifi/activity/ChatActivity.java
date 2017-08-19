package com.xugongming38.chatonwifi.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.xugongming38.chatonwifi.R;
import com.xugongming38.chatonwifi.adapter.ChatListAdapter;
import com.xugongming38.chatonwifi.data.ChatMessage;
import com.xugongming38.chatonwifi.interfaces.ReceiveMsgListener;
import com.xugongming38.chatonwifi.net.NetTcpFileSendThread;
import com.xugongming38.chatonwifi.utils.IpMessageConst;
import com.xugongming38.chatonwifi.utils.IpMessageProtocol;
import com.xugongming38.chatonwifi.utils.UsedConst;

import static com.xugongming38.chatonwifi.activity.BaseActivity.netThreadHelper;
import static com.xugongming38.chatonwifi.activity.BaseActivity.sendEmptyMessage;

public class ChatActivity extends BaseActivity implements OnClickListener,ReceiveMsgListener {
    private TextView chat_name;			//名字及IP
    private TextView chat_mood;			//组名
    private Button chat_quit;			//退出按钮
    private ListView chat_list;			//聊天列表
    private EditText chat_input;		//聊天输入框
    private Button chat_send;			//发送按钮
    private Button file_send;			//发送文件

    private List<ChatMessage> msgList;	//用于显示的消息list
    private String receiverName;			//要接收本activity所发送的消息的用户名字
    private String receiverIp;			//要接收本activity所发送的消息的用户IP
    private String receiverGroup;			//要接收本activity所发送的消息的用户组名
    private ChatListAdapter adapter;	//ListView对应的adapter
    private String selfName;
    private String selfGroup;

    private final static int MENU_ITEM_SENDFILE = Menu.FIRST;	//发送文件
    private final static int MENU_ITEM_EXIT = Menu.FIRST + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        findViews();

//		netThreadHelper = NetThreadHelper.newInstance();
        msgList = new ArrayList<ChatMessage>();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        receiverName = bundle.getString("receiverName");
        receiverIp = bundle.getString("receiverIp");
        receiverGroup = bundle.getString("receiverGroup");
        selfName = UsedConst.name;
        selfGroup = "android";

        chat_name.setText(receiverName + "(" + receiverIp + ")");
        chat_mood.setText("组名：" + receiverGroup);
        chat_quit.setOnClickListener(this);
        chat_send.setOnClickListener(this);
        file_send.setOnClickListener(this);
        Iterator<ChatMessage> it = netThreadHelper.getReceiveMsgQueue().iterator();
        while(it.hasNext()){	//循环消息队列，获取队列中与本聊天activity相关信息
            ChatMessage temp = it.next();
            //若消息队列中的发送者与本activity的消息接收者IP相同，则将这个消息拿出，添加到本activity要显示的消息list中
            if(receiverIp.equals(temp.getSenderIp())){
                msgList.add(temp);	//添加到显示list
                it.remove();		//将本消息从消息队列中移除
            }
        }

        adapter = new ChatListAdapter(this, msgList);
        chat_list.setAdapter(adapter);

        netThreadHelper.addReceiveMsgListener(this);	//注册到listeners
    }

    private void findViews(){
//		chat_item_head = (ImageView) findViewById(R.id.chat_item_head);
        chat_name = (TextView) findViewById(R.id.chat_name);
        chat_mood = (TextView) findViewById(R.id.chat_mood);
        chat_quit = (Button) findViewById(R.id.chat_quit);
        chat_list = (ListView) findViewById(R.id.chat_list);
        chat_input = (EditText) findViewById(R.id.chat_input);
        chat_send = (Button) findViewById(R.id.chat_send);
        file_send = (Button) findViewById(R.id.add_file);
    }

    @Override
    public void processMessage(Message msg) {
        // TODO Auto-generated method stub
        switch(msg.what){
            case IpMessageConst.IPMSG_SENDMSG:
                adapter.notifyDataSetChanged();	//刷新ListView
                break;

            case IpMessageConst.IPMSG_RELEASEFILES:{ //拒绝接受文件,停止发送文件线程
                if(NetTcpFileSendThread.server != null){
                    try {
                        NetTcpFileSendThread.server.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            break;

            case UsedConst.FILESENDSUCCESS:{	//文件发送成功
                makeTextShort("文件发送成功");
            }
            break;


        }	//end of switch
    }

    @Override
    public boolean receive(ChatMessage msg) {
        // TODO Auto-generated method stub
        if(receiverIp.equals(msg.getSenderIp())){	//若消息与本activity有关，则接收
            msgList.add(msg);	//将此消息添加到显示list中
            sendEmptyMessage(IpMessageConst.IPMSG_SENDMSG); //使用handle通知，来更新UI
            BaseActivity.playMsg();
            return true;
        }

        return false;
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        //一定要移除，不然信息接收会出现问题
        netThreadHelper.removeReceiveMsgListener(this);
        super.finish();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if(v == chat_send){
            sendAndAddMessage();
        }else if(v == chat_quit){
            finish();
        }else if(v==file_send){
            Intent intent = new Intent(this, FileActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    /**
     * 发送消息并将该消息添加到UI显示
     */
    private void sendAndAddMessage(){
        String msgStr = chat_input.getText().toString().trim();
        if(!"".equals(msgStr)){
            //发送消息
            final IpMessageProtocol sendMsg = new IpMessageProtocol();
            sendMsg.setVersion(String.valueOf(IpMessageConst.VERSION));
            sendMsg.setSenderName(selfName);
            sendMsg.setSenderHost(selfGroup);
            sendMsg.setCommandNo(IpMessageConst.IPMSG_SENDMSG);
            sendMsg.setAdditionalSection(msgStr);

            try {
                final InetAddress sendto = InetAddress.getByName(receiverIp);
                if(sendto != null){
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            netThreadHelper.sendUdpData(sendMsg.getProtocolString() + "\0", sendto, IpMessageConst.PORT);
                        }
                    } ).start();
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                Log.e("MyFeiGeChatActivity", "发送地址有误");
            }

                //netThreadHelper.sendUdpData(sendMsg.getProtocolString() + "\0", sendto, IpMessageConst.PORT);

            //添加消息到显示list
            ChatMessage selfMsg = new ChatMessage("localhost", selfName, msgStr, new Date());
            selfMsg.setSelfMsg(true);	//设置为自身消息
            msgList.add(selfMsg);

        }else{
            makeTextShort("不能发送空内容");
        }

        chat_input.setText("");
        adapter.notifyDataSetChanged();//更新UI
    }


	/*
	 * 菜单键呼出文件发送或者退出的按钮--已删除
	 */


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("..", "fanhuizhong1");
        if(resultCode == RESULT_OK){
            Log.e("..", "zhengque");
            //得到发送文件的路径
            Bundle bundle = data.getExtras();


            String filePaths = bundle.getString("filePaths");	//附加文件信息串,多个文件使用"\0"进行分隔
//			Toast.makeText(this, filePaths, Toast.LENGTH_SHORT).show();
            Log.e("..", filePaths);
            String[] filePathArray = filePaths.split("\0");


            //发送传送文件UDP数据报
            final IpMessageProtocol sendPro = new IpMessageProtocol();
            sendPro.setVersion("" +IpMessageConst.VERSION);
            sendPro.setCommandNo(IpMessageConst.IPMSG_SENDMSG | IpMessageConst.IPMSG_FILEATTACHOPT);
            sendPro.setSenderName(selfName);
            sendPro.setSenderHost(selfGroup);
            String msgStr = "";	//发送的消息

            StringBuffer additionInfoSb = new StringBuffer();	//用于组合附加文件格式的sb
            for(String path:filePathArray){
                File file = new File(path);
                additionInfoSb.append("0:");
                additionInfoSb.append(file.getName() + ":");
                additionInfoSb.append(Long.toHexString(file.length()) + ":");		//文件大小十六进制表示
                additionInfoSb.append(Long.toHexString(file.lastModified()) + ":");	//文件创建时间，现在暂时已最后修改时间替代
                additionInfoSb.append(IpMessageConst.IPMSG_FILE_REGULAR + ":");
                byte[] bt = {0x07};		//用于分隔多个发送文件的字符
                String splitStr = new String(bt);
                additionInfoSb.append(splitStr);
            }

            sendPro.setAdditionalSection(msgStr + "\0" + additionInfoSb.toString() + "\0");


            try {
                final InetAddress sendto = InetAddress.getByName(receiverIp);
                if(sendto != null){
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            netThreadHelper.sendUdpData(sendPro.getProtocolString(), sendto, IpMessageConst.PORT);
                        }
                    } ).start();
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                Log.e("MyFeiGeChatActivity", "发送地址有误");
            }



            //监听2425端口，准备接受TCP连接请求
            Thread netTcpFileSendThread = new Thread(new NetTcpFileSendThread(filePathArray));
            netTcpFileSendThread.start();	//启动线程
        }
    }


}
