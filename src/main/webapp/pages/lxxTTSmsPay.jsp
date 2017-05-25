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
	jQuery("#payment_but").live('click', function(event){
		var _root = jQuery("#_root").val();
		var _url = _root+"/lxx/smsPayV2";
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
		jQuery("#sms_pay_frm").ajaxSubmit(options);
		
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
		<a href="<%=request.getContextPath()%>/api/lxxTTGetSms" >获取验证码接口</a>
		<a href="<%=request.getContextPath()%>/api/lxxTTSmsPay" id="current_nav">验证码支付接口</a>
	</div>
	
	<div class="api_from_div">
		<jsp:include page="lxxTTSmsPayBase.jsp" />
		
		<div id="alert_message"></div>
		<div class="tab_title">在线调试，输入参数后，请单击“调用”按钮。</div>
		
	<form id="sms_pay_frm" name="sms_pay_frm" method="post">
		<table class="tab2" cellpadding="0" border="0" cellspacing="0" >
			<thead>
				<tr>
					<th width="10%">参数</th>
					<th align="left">值</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td align="right"><font color="red">*</font>verifycode：</td>
					<td><input type="text" id="verifycode" name="verifycode" /></td>
				</tr>
				<tr>
					<td align="right"><font color="red">*</font>apiKey：</td>
					<td><input type="text" id="apiKey" name="apiKey" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>apiPwd：</td>
					<td><input type="text" id="apiPwd" name="apiPwd" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>orderid：</td>
					<td><input type="text" id="orderid" name="orderid" /></td>
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
					<td>verifycode</td>
					<td>string</td>
					<td>必录</td>
					<td>验证码</td>
				</tr>
				<tr>
					<td>channelcode</td>
					<td>string</td>
					<td>必录</td>
					<td>渠道代码</td>
				</tr>
				
				<tr>
					<td>password</td>
					<td>string</td>
					<td>必录</td>
					<td>渠道密码</td>
				</tr>
				
				<tr>
					<td>orderid</td>
					<td>string</td>
					<td>必录</td>
					<td>订单号</td>
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
					<td>resultCode</td>
					<td>string</td>
					<td>必选</td>
					<td>0成功，非0失败。</td>
				</tr>
				
				<tr>
					<td>resultMsg</td>
					<td>string</td>
					<td>必选</td>
					<td>结果描述。</td>
				</tr>
				
				<tr>
					<td>orderId</td>
					<td>string</td>
					<td>必选</td>
					<td>订单号</td>
				</tr>
				
				
				<tr>
					<td>响应消息体格式：</td>
					<td colspan="3">
<pre>
{” resultCode”:”0”,” resultMsg”:”xxxx” , ”orderId”:”XXX”}
</pre>
					</td>
				</tr>
				
			</tbody>
		</table>
	</div>
</body>
</html>