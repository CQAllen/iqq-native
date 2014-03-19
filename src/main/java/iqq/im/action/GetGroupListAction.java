/**
 * 
 */
package iqq.im.action;

import iqq.im.QQActionListener;
import iqq.im.QQException;
import iqq.im.bean.QQGroup;
import iqq.im.core.QQConstants;
import iqq.im.core.QQContext;
import iqq.im.core.QQSession;
import iqq.im.core.QQStore;
import iqq.im.event.QQActionEvent;
import iqq.im.http.QQHttpRequest;
import iqq.im.http.QQHttpResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 获取群列表名称
 * 
 * @author ChenZhiHui
 * @create-date 2013-2-21
 */
public class GetGroupListAction extends AbstractHttpAction {
	private static final Logger LOG = Logger
			.getLogger(GetGroupListAction.class);

	public GetGroupListAction(QQContext context, QQActionListener listener) {
		super(context, listener);
	}

	@Override
	public QQHttpRequest onBuildRequest() throws QQException, JSONException {
		QQSession session = getContext().getSession();

		JSONObject json = new JSONObject();
		json.put("vfwebqq", session.getVfwebqq()); 

		QQHttpRequest req = createHttpRequest("POST",
				QQConstants.URL_GET_GROUP_NAME_LIST);
		req.addPostValue("r", json.toString());

		req.addHeader("Referer", QQConstants.REFFER);

		return req;
	}

	@Override
	protected void onHttpStatusOK(QQHttpResponse response) throws QQException,
			JSONException {
		// {"retcode":0,"result":{"gmasklist":[{"gid":1000,"mask":0},{"gid":1638195794,"mask":0},{"gid":321105219,"mask":0}],
		// "gnamelist":[{"flag":16777217,"name":"iQQ","gid":1638195794,"code":2357062609},{"flag":1048577,"name":"iQQ核心开发区","gid":321105219,"code":640215156}],"gmarklist":[]}}
		QQStore store = getContext().getStore();
		JSONObject json = new JSONObject(response.getResponseString());
		
		int retcode = json.getInt("retcode");
		if (retcode == 0) {
			// 处理好友列表
			JSONObject results = json.getJSONObject("result");
			JSONArray groupJsonList = results.getJSONArray("gnamelist");	// 群列表
			JSONArray groupMaskJsonList = results.getJSONArray("gmasklist");	//禁止接收群消息标志：正常 0， 接收不提醒 1， 完全屏蔽 2

			for (int i = 0; i < groupJsonList.length(); i++) {
				JSONObject groupJson = groupJsonList.getJSONObject(i);
				QQGroup group = new QQGroup();
				group.setGin(groupJson.getLong("gid"));
				group.setCode(groupJson.getLong("code"));
				group.setFlag(groupJson.getInt("flag"));
				group.setName(groupJson.getString("name"));
				//添加到Store
				store.addGroup(group);
			}
			
			for(int i = 0; i < groupMaskJsonList.length(); i++) {
				JSONObject maskObj = groupMaskJsonList.getJSONObject(i);
				long gid = maskObj.getLong("gid");
				int mask = maskObj.getInt("mask");
				QQGroup group = store.getGroupByGin(gid);
				if(group != null) {
					group.setMask(mask);
				}
			}

			notifyActionEvent(QQActionEvent.Type.EVT_OK, store.getGroupList());

		} else {
			LOG.warn("unknown retcode: " + retcode);
			notifyActionEvent(QQActionEvent.Type.EVT_ERROR, null);
		}

	}

}
