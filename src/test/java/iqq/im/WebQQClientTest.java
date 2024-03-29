/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Project  : WebQQCoreAsync
 * Package  : iqq.im
 * File     : WebQQClientTest.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2012-9-6
 * License  : Apache License 2.0 
 */
package iqq.im;

import iqq.im.actor.ThreadActorDispatcher;
import iqq.im.bean.QQBuddy;
import iqq.im.bean.QQCategory;
import iqq.im.bean.QQDiscuz;
import iqq.im.bean.QQEmail;
import iqq.im.bean.QQGroup;
import iqq.im.bean.QQGroupSearchList;
import iqq.im.bean.QQMsg;
import iqq.im.bean.QQStatus;
import iqq.im.bean.content.ContentItem;
import iqq.im.bean.content.FaceItem;
import iqq.im.bean.content.OffPicItem;
import iqq.im.bean.content.TextItem;
import iqq.im.core.QQConstants;
import iqq.im.event.QQActionEvent;
import iqq.im.event.QQActionEvent.Type;
import iqq.im.event.QQNotifyEvent;
import iqq.im.event.QQNotifyEventArgs;
import iqq.im.event.QQNotifyHandler;
import iqq.im.event.QQNotifyHandlerProxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Client测试类
 * 
 * @author solosky <solosky772@qq.com>
 * 
 */
public class WebQQClientTest {
	
	QQClient client;
	
	public WebQQClientTest(String user, String pwd){
		client = new WebQQClient(user,pwd, new QQNotifyHandlerProxy(this), new ThreadActorDispatcher());
	}
	
	/**
	 * 新邮件通知
	 * 
	 * @param event
	 * @throws QQException
	 */
	@QQNotifyHandler(QQNotifyEvent.Type.EMAIL_NOTIFY)
	public void processEmailMsg(QQNotifyEvent event) throws QQException {
		List<QQEmail> list = (List<QQEmail>) event.getTarget();
		for(QQEmail mail : list) {
			System.out.println("邮件: " + mail.getSubject());
		}
	}
	
	/**
	 * 聊天消息通知
	 * 
	 * @param event
	 * @throws QQException
	 */
	@QQNotifyHandler(QQNotifyEvent.Type.CHAT_MSG)
	public void processBuddyMsg(QQNotifyEvent event) throws QQException{
		QQMsg msg = (QQMsg) event.getTarget();
		
		System.out.println("[消息] " + msg.getFrom().getNickname() + "说:" + msg.packContentList());
		System.out.print("消息内容: ");
		List<ContentItem> items = msg.getContentList();
		for(ContentItem item : items) {
			if(item.getType() == ContentItem.Type.FACE) {
				System.out.print(" Face:" + ((FaceItem)item).getId());
			}else if(item.getType() == ContentItem.Type.OFFPIC) {
				System.out.print(" Picture:" + ((OffPicItem)item).getFilePath());
			}else if(item.getType() == ContentItem.Type.TEXT) {
				System.out.print(" Text:" + ((TextItem)item).getContent());
			}
		}
		System.out.println();
	}
	
	/**
	 * 被踢下线通知
	 * 
	 * @param event
	 */
	@QQNotifyHandler(QQNotifyEvent.Type.KICK_OFFLINE)
	protected void processKickOff(QQNotifyEvent event){
		System.out.println("被踢下线: " + (String)event.getTarget());
	}
	
	/**
	 * 需要验证码通知
	 * 
	 * @param event
	 * @throws IOException
	 */
	@QQNotifyHandler(QQNotifyEvent.Type.CAPACHA_VERIFY)
	protected void processVerify(QQNotifyEvent event) throws IOException{
		QQNotifyEventArgs.ImageVerify verify = (QQNotifyEventArgs.ImageVerify) event.getTarget();
		ImageIO.write(verify.image, "png", new File("verify.png"));
		System.out.println(verify.reason);
		System.out.print("请输入在项目根目录下verify.png图片里面的验证码:");
		String code = new BufferedReader(new InputStreamReader(System.in)).readLine();
		client.submitVerify(code, event);
	}

	/**
	 * 程序入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		WebQQClientTest test = new WebQQClientTest("917362009", "chen01234");
		test.login();
	}

	/**
	 * 登录
	 */
	public void login() {
		final QQActionListener listener = new QQActionListener() {
			public void onActionEvent(QQActionEvent event) {
				System.out.println("LOGIN_STATUS:" + event.getType() + ":" + event.getTarget());
				if (event.getType() == Type.EVT_OK) {
					//到这里就算是登录成功了
					
					//获取下用户信息
					client.getUserInfo(client.getAccount(), new QQActionListener() {
						public void onActionEvent(QQActionEvent event) {
							System.out.println("LOGIN_STATUS:"+ event.getType() + ":" + event.getTarget());
						}
					});
				
					// 获取好友列表..TODO.
					// 不一定调用，可能会有本地缓存
					client.getBuddyList(new QQActionListener() {

						@Override
						public void onActionEvent(QQActionEvent event) {
							// TODO Auto-generated method stub
							System.out.println("******** " + event.getType()
									+ " ********");
							if (event.getType() == QQActionEvent.Type.EVT_OK) {
								System.out.println("******** 好友列表  ********");
								List<QQCategory> qqCategoryList = (List<QQCategory>) event
										.getTarget();

								for (QQCategory c : qqCategoryList) {
									System.out.println("分组名称:" + c.getName());
									List<QQBuddy> buddyList = c.getBuddyList();
									for (QQBuddy b : buddyList) {
										System.out.println("---- QQ nick:"
												+ b.getNickname()
												+ " markname:"
												+ b.getMarkname() + " uin:"
												+ b.getUin() + " isVip:"
												+ b.isVip() + " vip_level:"
												+ b.getVipLevel());
									}

								}
							} else if (event.getType() == QQActionEvent.Type.EVT_ERROR) {
								System.out.println("** 好友列表获取失败，处理重新获取");
							}
						}
					});
					// 获取群列表
					client.getGroupList(new QQActionListener() {
						
						@Override
						public void onActionEvent(QQActionEvent event) {
							if(event.getType() == Type.EVT_OK) {
								for(QQGroup g : client.getGroupList()) {
									client.getGroupInfo(g, null);
									System.out.println("Group: " + g.getName());
								}
							}
						}
					});
					// 获取讨论组列表
					client.getDiscuzList(new QQActionListener() {
						
						@Override
						public void onActionEvent(QQActionEvent event) {
							if(event.getType() == Type.EVT_OK) {
								for(QQDiscuz d : client.getDiscuzList()) {
									client.getDiscuzInfo(d, null);
									System.out.println("Discuz: " + d.getName());
								}
							}
						}
					});
					
					// 查群测试
					QQGroupSearchList list = new QQGroupSearchList();
					list.setKeyStr("QQ");
					client.searchGroupGetList(list, new QQActionListener() {
						@Override
						public void onActionEvent(QQActionEvent event) {
							if(event.getType() == Type.EVT_OK) {
								
							}
						}
					});
					
					//启动轮询时，需要获取所有好友、群成员、讨论组成员
					//所有的逻辑完了后，启动消息轮询
					client.beginPollMsg();
				}
			}
		};
		
		String ua = "Mozilla/5.0 (@os.name; @os.version; @os.arch) AppleWebKit/537.36 (KHTML, like Gecko) @appName Safari/537.36";
		ua = ua.replaceAll("@appName", QQConstants.USER_AGENT);
		ua = ua.replaceAll("@os.name", System.getProperty("os.name"));
		ua = ua.replaceAll("@os.version", System.getProperty("os.version"));
		ua = ua.replaceAll("@os.arch", System.getProperty("os.arch"));
		client.setHttpUserAgent(ua);
		client.login(QQStatus.ONLINE, listener);
	}

}
