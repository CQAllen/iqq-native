package iqq.im.action;

import iqq.im.QQActionListener;
import iqq.im.QQException;
import iqq.im.bean.QQGroup;
import iqq.im.core.QQConstants;
import iqq.im.core.QQContext;
import iqq.im.core.QQSession;
import iqq.im.event.QQActionEvent;
import iqq.im.http.QQHttpRequest;
import iqq.im.http.QQHttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 获取QQ账号
 * @author ChenZhiHui
 * @create-time 2013-2-23
 */
public class GetGroupAccoutAction extends AbstractHttpAction {

	private QQGroup group;

	public GetGroupAccoutAction(QQContext context, QQActionListener listener, QQGroup group) {
		super(context, listener);
		this.group = group;
	}

	@Override
	protected QQHttpRequest onBuildRequest() throws QQException, JSONException {
		QQSession session = getContext().getSession();
		// tuin=4245757755&verifysession=&type=1&code=&vfwebqq=**&t=1361631644492
		QQHttpRequest req = createHttpRequest("GET",
				QQConstants.URL_GET_USER_ACCOUNT);
		req.addGetValue("tuin", group.getCode() + "");
		req.addGetValue("vfwebqq", session.getVfwebqq());
		req.addGetValue("t", System.currentTimeMillis() / 1000 + "");
		req.addGetValue("verifysession", ""); // 验证码？？
		req.addGetValue("type", 4 + "");
		req.addGetValue("code", "");

		req.addHeader("Referer", QQConstants.REFFER);
		return req;
	}

	@Override
	protected void onHttpStatusOK(QQHttpResponse response) throws QQException,
			JSONException {
		JSONObject json = new JSONObject(response.getResponseString());
		if (json.getInt("retcode") == 0) {
			JSONObject obj = json.getJSONObject("result");
			group.setGid(obj.getLong("account"));
		}
		
		notifyActionEvent(QQActionEvent.Type.EVT_OK, group);
	}

}
