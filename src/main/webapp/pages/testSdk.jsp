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
		var _url = _root+"/sdk/getTasks";
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
		<a href="<%=request.getContextPath()%>/api/testSdk" id="current_nav">SDK测试接口</a>
	</div>
	
	<div class="api_from_div">
		<jsp:include page="paymentTotalBase.jsp" />
		
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
					<td align="right"><font color="red">*</font>productCode：</td>
					<td><input type="text" id="productCode" name="productCode" /></td>
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
					<td align="right"><font color="red">*</font>imsi：</td>
					<td><input type="text" id="imsi" name="imsi" /></td>
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
					<td>productCode</td>
					<td>string</td>
					<td>必录</td>
					<td>商品id</td>
				</tr>
				
				<tr>
					<td>apiKey</td>
					<td>string</td>
					<td>必录</td>
					<td>渠道号</td>
				</tr>
				
				<tr>
					<td>apiPwd</td>
					<td>string</td>
					<td>必录</td>
					<td>渠道password</td>
				</tr>
				
				<tr>
					<td>imsi</td>
					<td>string</td>
					<td>选录</td>
					<td>SIM卡串号</td>
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
					<td>可选</td>
					<td>结果描述。</td>
				</tr>
				
				<tr>
					<td>sms</td>
					<td>string</td>
					<td>必选</td>
					<td>短信内容</td>
				</tr>
				
				<tr>
					<td>accessno</td>
					<td>string</td>
					<td>必选</td>
					<td>短信发送至</td>
				</tr>
				
				<tr>
					<td>orderId</td>
					<td>string</td>
					<td>必选</td>
					<td>计费平台生成唯一订单号</td>
				</tr>
				
				<tr>
					<td>confirmCode</td>
					<td>string</td>
					<td>必选</td>
					<td>验证码短信内容</td>
				</tr>
				
				<tr>
					<td>confirmAccessno</td>
					<td>string</td>
					<td>必选</td>
					<td>验证码短信发送至</td>
				</tr>
				
				<tr>
					<td>响应消息体格式：</td>
					<td colspan="3">
<pre>
{
"resultCode":"0",
"resultMsg":"请求成功",
"sms":"111860000000011454214376231423463114733922711335403290fd21e93fffe9b000",
"accessno":"106566660020",
"orderId":"14376231423463114733922711335403",
"confirmCode":"860000",
"confirmAccessno":"106566660020"
}
</pre>
					</td>
				</tr>
				
			</tbody>
		</table>
	</div>
</body>
</html>