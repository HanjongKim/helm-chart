<%@ page import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- 위 3개의 메타 태그는 *반드시* head 태그의 처음에 와야합니다; 어떤 다른 콘텐츠들은 반드시 이 태그들 *다음에* 와야 합니다 -->
<title>부트스트랩 101 템플릿</title>

<!-- 합쳐지고 최소화된 최신 CSS -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">

<!-- 부가적인 테마 -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<!-- 합쳐지고 최소화된 최신 자바스크립트 -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/showdown/1.8.6/showdown.min.js"></script>
<style type="text/css">
.resize-100x100 {
    height: 100px;
    width: 100px;
}
#img_container {
     width:100%;
     height:100%;
}
 
#img_container img {
    display:block;
    margin-left:auto;
    margin-right:auto;
}
</style>
</head>
<body>
	<nav class="navbar navbar-default">
	<div class="container-fluid">
		<!-- Brand and toggle get grouped for better mobile display -->
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed"
				data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="/helm/chartList">Helm Chart</a>
		</div>

		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse"
			id="bs-example-navbar-collapse-1">
			<ul class="nav navbar-nav">
				<li class="active"><a href="/helm/chartList">Charts<span class="sr-only">(current)</span></a></li>
				<li><a href="/helm/deploymentList">Deployments <span class="sr-only">(current)</span></a></li>
				<!-- <li><a href="#">Link</a></li> -->
				<!-- <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Dropdown <span class="caret"></span></a>
          <ul class="dropdown-menu" role="menu">
            <li><a href="#">Action</a></li>
            <li><a href="#">Another action</a></li>
            <li><a href="#">Something else here</a></li>
            <li class="divider"></li>
            <li><a href="#">Separated link</a></li>
            <li class="divider"></li>
            <li><a href="#">One more separated link</a></li>
          </ul>
        </li>
      </ul> -->
				<!-- <form class="navbar-form navbar-left" role="search">
        <div class="form-group">
          <input type="text" class="form-control" placeholder="Search">
        </div>
        <button type="submit" class="btn btn-default">Submit</button>
      </form> -->
				<!-- <ul class="nav navbar-nav navbar-right">
        <li><a href="#">Link</a></li>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Dropdown <span class="caret"></span></a>
          <ul class="dropdown-menu" role="menu">
            <li><a href="#">Action</a></li>
            <li><a href="#">Another action</a></li>
            <li><a href="#">Something else here</a></li>
            <li class="divider"></li>
            <li><a href="#">Separated link</a></li>
          </ul>
        </li>
      </ul> -->
		</div>
		<!-- /.navbar-collapse -->
	</div>
	</nav>
	<div class="container">
	<div class="row">
	  <div class="col-md-9">
	  	<div class="panel panel-default">
		  	<div class="page-header">
			  <h2>README.md</h2>
			</div>
			<div id="img_container" class="panel-body">
		  		${readMe }
		  	</div>
		  </div>
	  </div>
	  <div class="col-md-3">
	  	<div class="panel panel-default">
		  <div id="img_container" class="panel-body">
		    <c:if test="${chart['CHART_ICON'] eq ''}">
				<img class="img-thumbnail resize-100x100" src="https://raw.githubusercontent.com/funnylab/helm-repo/master/noimage.png" alt="${CHART_DETAIL['ENT_NAME']}">
			</c:if>
			<c:if test="${chart['CHART_ICON'] ne ''}">
				<img class="img-thumbnail resize-100x100" src="${CHART_DETAIL['CHART_ICON']}" alt="${CHART_DETAIL['ENT_NAME']}">
			</c:if>
		  	<center><h3 style="color: red;">${CHART_DETAIL['CHART_NAME']}</h3>
	  		<h5 style="text-decoration: underline; color: blue; font-weight: bold;">${CHART_DETAIL['CHART_VERSION']}</h5></center>
	  		<h5 style="font-weight: bold;">Description : </h5>
	  		${CHART_DETAIL['CHART_DESC']}
		  </div>
		</div>
		<p>
		  <a href="/helm/chartInstallDetail/${CHART_DETAIL['CHART_NAME']}/${CHART_DETAIL['CHART_VERSION']}" class="btn btn-primary btn-lg btn-block" role="button">Helm Install</a>
		</p>
	  	<h3>Versions</h3>
	  	<ul class="list-group">
          	<c:forEach items="${CHART_VERSIONS}" var="version">
		  		<li class="list-group-item"><a href="/helm/chartDetail/${CHART_DETAIL['CHART_NAME']}/${version}">${version}</a></li>
		  	</c:forEach>
		</ul>
	  </div>
	</div>
	</div>
	<nav class="navbar navbar-default navbar-fixed-bottom">
	<div class="container">...</div>
	</nav>
</body>
</html>