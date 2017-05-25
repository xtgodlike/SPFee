<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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
		jQuery("#payment_but").live('click', function(event) {
			var _root = jQuery("#_root").val();
			var _url = _root + "/cb/getAck";
			jQuery("#alert_message").html();

			var options = {
				url : _url,
				success : function(data) {
					if (null == data || '' == data) {
						jQuery("#alert_message").html("未查询到数据！");
					} else {
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
		<a href="<%=request.getContextPath()%>/api/paymentSmsTotal"
			id="current_nav">创贝回调接口</a>
	</div>

	<div class="api_from_div">
		

		<div id="alert_message"></div>
		<div class="tab_title">在线调试，输入参数后，请单击“调用”按钮。</div>

		<form id="payment_code_frm" name="payment_code_frm" method="post">
			<table class="tab2" cellpadding="0" border="0" cellspacing="0">
				<thead>
					<tr>
						<th width="10%">参数</th>
						<th align="left">值</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td align="right"><font color="red">*</font>msg：</td>
						<td><input type="text" id="msg" name="msg" /></td>
					</tr>
					<tr>
						<td align="right"><font color="red">*</font>linkid：</td>
						<td><input type="text" id="linkid" name="linkid" /></td>
					</tr>

					<tr>
						<td align="right"><font color="red">*</font>spnumber：</td>
						<td><input type="text" id="spnumber" name="spnumber" /></td>
					</tr>

					<tr>
						<td align="right"><font color="red">*</font>mobile：</td>
						<td><input type="text" id="mobile" name="mobile" /></td>
					</tr>

					<tr>
						<td align="right"><font color="red">*</font>status：</td>
						<td><input type="text" id="status" name="status" /></td>
					</tr>

				</tbody>
				<tfoot>
					<tr>
						<td colspan="2"><input type="button" id="payment_but"
							name="payment_but" class="def_button" value="调用" /></td>
					</tr>
				</tfoot>
			</table>
		</form>
</body>
</html>