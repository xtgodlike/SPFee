<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="pragma" content="no-cache" /> 
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate" /> 
<jsp:include page="base.jsp" />
<title>计费平台开发者中心</title>
<script type="text/javascript">
jQuery(document).ready(function() {
	//提交签报
	jQuery("#payment_but").live('click', function(event){
		var _root = jQuery("#_root").val();
		var _url = _root+"/yk/ack";
		jQuery("#alert_message").html();
		
		var options = {
			url: _url,
	        success: function (data) {
	           if(null==data || ''==data){
	        	   jQuery("#alert_message").html("未查询到数据！");
	           }else{
	        	   var str = JSON.stringify(data); 
	        	   jQuery("#alert_message").html(str);
	           }
	           jQuery("#alert_message").show();
	        }
	    };
		jQuery("#payment_code_frm").ajaxSubmit(options);
		
	});	
});
</script>
</head>
<body>
	<jsp:include page="menu.jsp" />
	<div class="api_nav_title">
		 单击<a href="<%=request.getContextPath()%>/api/apiList">此处</a>，获取完整的操作列表。
	</div>
	
	<div class="nav_left">
		<a href="<%=request.getContextPath()%>/api/ykGetPaymentSms" id="current_nav">获取支付验证码</a>
		<a href="<%=request.getContextPath()%>/api/ykAck">通道回调</a>
	</div>
	
	<div class="api_from_div">
		<jsp:include page="mzZhPaymentBase.jsp" />
		
		<div id="alert_message"></div>
		<div class="tab_title">在线调试，输入参数后，请单击“调用”按钮。</div>
		
	<form id="payment_code_frm" name="payment_code_frm" method="post">
		<table class="tab2" cellpadding="0" border="0" cellspacing="0" >
			<thead>
				<tr>
					<th width="10%">参数</th>
					<th align="left">值</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td align="right"><font color="red">*</font>orderid：</td>
					<td><input type="text" id="orderid" name="orderid" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>linkid：</td>
					<td><input type="text" id="linkid" name="linkid" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>timestr：</td>
					<td><input type="text" id="timestr" name="timestr" /></td>
				</tr>
	
				<tr>
					<td align="right"><font color="red">*</font>status：</td>
					<td><input type="text" id="status" name="status" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>statusmsg：</td>
					<td><input type="text" id="statusmsg" name="statusmsg" /></td>
				</tr>
				<tr>
					<td align="right"><font color="red">*</font>mobile：</td>
					<td><input type="text" id="mobile" name="mobile" /></td>
				</tr>
				<tr>
					<td align="right"><font color="red">*</font>callbackdata：</td>
					<td><input type="text" id="callbackdata" name="callbackdata" /></td>
				</tr>
				<tr>
					<td align="right"><font color="red">*</font>imsi：</td>
					<td><input type="text" id="imsi" name="imsi" /></td>
				</tr>
				<tr>
					<td align="right"><font color="red">*</font>appid：</td>
					<td><input type="text" id="appid" name="appid" /></td>
				</tr>
				<tr>
					<td align="right"><font color="red">*</font>itemid：</td>
					<td><input type="text" id="itemid" name="itemid" /></td>
				</tr>
				<tr>
					<td align="right"><font color="red">*</font>feevalue：</td>
					<td><input type="text" id="feevalue" name="feevalue" /></td>
				</tr>
				<tr>
					<td align="right"><font color="red">*</font>signature：</td>
					<td><input type="text" id="signature" name="signature" /></td>
				</tr>
				
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="button" id="payment_but" name="payment_but" class="def_button" value="调用" />
					</td>
				</tr>
			</tfoot>
		</table>
	</form>
		
		<div class="tab_title">请求消息体参数</div>
		<table class="tab3" cellpadding="0" border="0" cellspacing="0" style="margin-bottom: 20px;" >
			<thead>
				<tr>
					<th align="left">参数名</th>
					<th align="left">类型</th>
					<th align="left">选取原则</th>
					<th align="left">说明</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>orderid</td>
					<td>string</td>
					<td>必录</td>
					<td>订单号</td>
				</tr>
				<tr>
					<td>linkid</td>
					<td>string</td>
					<td>必录</td>
					<td>流水号</td>
				</tr>
				
				<tr>
					<td>timestr</td>
					<td>string</td>
					<td>必录</td>
					<td>支付完成时间，本地时间戳，格式：20150115103315</td>
				</tr>
				
				<tr>
					<td>status</td>
					<td>string</td>
					<td>选录</td>
					<td>订单(短信)状态;2：成功；其他的为失败</td>
				</tr>
				<tr>
					<td>statusmsg</td>
					<td>string</td>
					<td>可选</td>
					<td>当订单状态status出现的错误状态不明白的，这个参数都会给出文字解释</td>
				</tr>
				<tr>
					<td>mobile</td>
					<td>string</td>
					<td>可选</td>
					<td>用户手机号码</td>
				</tr>
				<tr>
					<td>callbackdata</td>
					<td>string</td>
					<td>可选</td>
					<td>回传数据，由合作方自定义，我方不作处理，在计费结果通知中回传给合作方</td>
				</tr>
				<tr>
					<td>imsi</td>
					<td>string</td>
					<td>必选</td>
					<td>IMSI代表SIM卡的唯一标识</td>
				</tr>
				<tr>
					<td>appid</td>
					<td>string</td>
					<td>必选</td>
					<td>appid(应用ID)</td>
				</tr>
				<tr>
					<td>itemid</td>
					<td>string</td>
					<td>必选</td>
					<td>道具id</td>
				</tr>
				<tr>
					<td>feevalue</td>
					<td>string</td>
					<td>可选</td>
					<td>短信金额，单位元</td>
				</tr>
				<tr>
					<td>signature</td>
					<td>string</td>
					<td>必选</td>
					<td>signature值为MD5(orderid+timestr+appid+key)key是我们给渠道的密钥</td>
				</tr>
				
			</tbody>
		</table>
		<div class="tab_title" style="margin-top: 20px;">响应消息体参数</div>
		<table class="tab3" cellpadding="0" border="0" cellspacing="0" style="margin-bottom: 20px;" >
			<thead>
				<tr>
					<th align="left">参数名</th>
					<th align="left">类型</th>
					<th align="left">选取原则</th>
					<th align="left">说明</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>respcode</td>
					<td>string</td>
					<td>必选</td>
					<td>0成功，非0失败。</td>
				</tr>
				
				<tr>
					<td>msg</td>
					<td>string</td>
					<td>可选</td>
					<td>结果描述。</td>
				</tr>
								
				<tr>
					<td>响应消息体格式：</td>
					<td colspan="3">
<pre>
{

"respcode":"0",
"msg":"陈宫"
}
</pre>
					</td>
				</tr>
				
			</tbody>
		</table>
	</div>
</body>
</html>