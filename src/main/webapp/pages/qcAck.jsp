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
		var _url = _root+"/qc/getAck";
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
		<a href="<%=request.getContextPath()%>/api/qcAck" id="current_nav">通道回调</a>
	</div>
	
	<div class="api_from_div">
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
					<td align="right"><font color="red">*</font>Re_Phone：</td>
					<td><input type="text" id="Re_Phone" name="Re_Phone" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>LinkId：</td>
					<td><input type="text" id="LinkId" name="LinkId" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>Re_Stat：</td>
					<td><input type="text" id="Re_Stat" name="Re_Stat" /></td>
				</tr>
	
				<tr>
					<td align="right"><font color="red">*</font>Mo_Msg：</td>
					<td><input type="text" id="Mo_Msg" name="Mo_Msg" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>Mo_Port：</td>
					<td><input type="text" id="Mo_Port" name="Mo_Port" /></td>
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
					<td>Re_Phone</td>
					<td>string</td>
					<td>必录</td>
					<td>支付手机号码</td>
				</tr>
				<tr>
					<td>LinkId</td>
					<td>string</td>
					<td>必录</td>
					<td>订单号</td>
				</tr>
				
				<tr>
					<td>Re_Stat</td>
					<td>string</td>
					<td>必录</td>
					<td>支付状态(3成功；4失败)</td>
				</tr>
				
				<tr>
					<td>Mo_Msg</td>
					<td>string</td>
					<td>必录</td>
					<td>指令</td>
				</tr>
				<tr>
					<td>Mo_Port</td>
					<td>string</td>
					<td>必录</td>
					<td>端口</td>
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
					<td>Mo_Port</td>
					<td>string</td>
					<td>必选</td>
					<td>端口</td>
				</tr>
				
				<tr>
					<td>LinkId</td>
					<td>string</td>
					<td>必选</td>
					<td>订单号</td>
				</tr>
				
				<tr>
					<td>code</td>
					<td>string</td>
					<td>必选</td>
					<td>固定为0</td>
				</tr>
								
				<tr>
					<td>响应消息体格式：</td>
					<td colspan="3">
<pre>
{

"Mo_Port":"xxxx",
"LinkId":"xxxx",
"code":"0",
}
</pre>
					</td>
				</tr>
				
			</tbody>
		</table>
	</div>
</body>
</html>