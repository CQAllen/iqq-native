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
 * Project  : IQQ_V2.1
 * Package  : iqq.app.ui.content.chat.rich
 * File     : UIOffPicItem.java
 * Author   : solosky < solosky772@qq.com >
 * Created  : 2013-4-8
 * License  : Apache License 2.0 
 */
package iqq.app.ui.content.chat.rich;

import iqq.app.core.IMContext;
import iqq.app.core.IMService;
import iqq.app.event.IMEvent;
import iqq.app.event.IMEventType;
import iqq.app.service.IMEventService;
import iqq.app.service.IMSkinService;
import iqq.app.ui.content.chat.picloader.PicLoadListener;
import iqq.app.ui.content.chat.picloader.PicLoadState;
import iqq.app.ui.content.chat.picloader.PicLoader;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextPane;

import org.apache.log4j.Logger;

import com.alee.extended.image.WebImage;
import com.alee.extended.panel.GroupPanel;
import com.alee.extended.progress.WebProgressOverlay;

/**
 *
 * 图片对象，需子类实现
 *
 * @author solosky <solosky772@qq.com>
 *
 */
public class UILoaderPicItem extends UIBasicItem {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(UILoaderPicItem.class);
	private PicLoader picLoader;
	public UILoaderPicItem(PicLoader picLoader){
		this.picLoader = picLoader; 
	}
	
	public PicLoader getPicLoader() {
		return picLoader;
	}

	@Override
	public void setContext(IMContext context) {
		super.setContext(context);
		this.picLoader.setContext(context);
	}


	@Override
	public void insertTo(JTextPane pane) throws Exception {
		pane.insertComponent(new PicPane());
	}
	
	private class PicPane extends GroupPanel implements UIRichComponent, PicLoadListener{
		private static final long serialVersionUID = 1L;
		public WebProgressOverlay progressOverlay;
		
		public PicPane() throws IOException{
			super();
			
			IMSkinService skins = getContext().getSerivce(IMService.Type.SKIN);
			JLabel background = new JLabel();
			background.setIcon(new ImageIcon(skins.getBufferedImage("defaultPic")));
			progressOverlay = new WebProgressOverlay ();
			progressOverlay.setProgressColor ( Color.WHITE );
			progressOverlay.setConsumeEvents ( false );
			progressOverlay.setComponent(background);
			progressOverlay.setShowLoad(true);
			this.add(progressOverlay);
			
			progressOverlay.addMouseListener(new MouseAdapter() {
				public void mousePressed ( MouseEvent e ){
					if(picLoader!= null && picLoader.getPic() != null && e.getClickCount() == 2){
						IMEventService eventHub = getContext().getSerivce(IMService.Type.EVENT);
						eventHub.broadcast(new IMEvent(IMEventType.SHOW_PIC_PREVIEW, picLoader.getPic()));
					}
	            }
			});
			picLoader.addListener(this);
			//TODO ..判断是否多次加载
			picLoader.loadPic();
		}

		@Override
		public UIRichItem getRichItem() {
			return UILoaderPicItem.this;
		}

		@Override
		public void onProgress(long current, long total) {
			//TODO ...显示加载或者上传进度
			LOG.debug("UIPicItem onProgress:" + current + "/" + total);
		}

		@Override
		public void onError(Throwable t) {
			IMSkinService skins = getContext().getSerivce(IMService.Type.SKIN);
            progressOverlay.setComponent(new WebImage(skins.getBufferedImage("badPic")));
            progressOverlay.setShowLoad(false);
			LOG.warn("load pic Error!", t);
			picLoader.removeListener(this);
		}

		@Override
		public void onSuccess(Image pic) {
			JLabel background = new JLabel();
			background.setIcon(new ImageIcon(pic));
			progressOverlay.setComponent(background);
			progressOverlay.setShowLoad(false);
			picLoader.removeListener(this);
		}
	}
}
