<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<!doctype html>
<html>
    <head>
        <r:require module='jqueryui'/>
        <r:require module='bootstrap_responsive'/>
        <meta name="layout" content="adminLayout"/>
        <title>Soils to Satellites - Admin - Matrix</title>
    </head>

    <body>
        <content tag="pageTitle">Matrix</content>
        <content tag="adminButtonBar">
        </content>

        <script type="text/javascript">

            $(document).ready(function() {
                $("#btnDelete").click(function(e) {
                    e.preventDefault();
                    if (confirm("Are you sure you want to delete this question?")) {
                        window.location = "${createLink(action:"deleteQuestion", params:[questionId:question.id])}";
                    }
                });
            });

        </script>
        <content tag="adminButtonBar">
            <a class="btn btn-small" href="${createLink(controller: 'admin', action:'matrix')}">Back to matrix</a>
        </content>
        <g:form class="form-horizontal" action="updateQuestion">
            <div class="well well-small">
                <h5>Edit Question</h5>

                <g:hiddenField name="questionId" value="${question.id}" />

                <div class="control-group">
                    <label class="control-label" for='question'>Question:</label>
                    <div class="controls">
                        <g:textField class="input-xlarge" name="question" placeholder="Question text" value="${question.text}"/>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for='description'>Description:</label>

                    <div class="controls">
                        <g:textField class="input-xlarge" name="description" placeholder="Description" value="${question.description}"/>
                    </div>
                </div>

                <div class="control-group">
                    <div class="controls">
                        <g:submitButton class="btn btn-primary" name="submit" value='Update'/>
                        <button id="btnDelete" type="button" class="btn btn-danger">Delete</button>
                    </div>
                </div>

            </div>

        </g:form>

    </body>
</html>