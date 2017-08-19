package com.xugongming38.chatonwifi.interfaces;

import com.xugongming38.chatonwifi.data.ChatMessage;

/**
 * 接收消息监听的listener接口
 * Created by dell on 2017/6/24.
 */

public interface ReceiveMsgListener {
    public boolean receive(ChatMessage msg);

}