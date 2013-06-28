<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<!doctype html>
<html>
    <head>
        <r:require module='jqueryui'/>
        <r:require module='bootstrap_responsive'/>
        <meta name="layout" content="detail"/>
        <title>Extract Data from Study Location Visits</title>
    </head>

    <body>

        <style type="text/css">

            .questionDescription {
                font-style: italic;
                font-size: 0.9em;
            }

        </style>

        <script type="text/javascript">

            $(document).ready(function () {
            });

        </script>

        <div class="container">
            <legend>
                <table style="width:100%">
                    <tr>
                        <td>
                            <sts:homeBreadCrumb />
                            <sts:navSeperator/>
                            <span class="sts-breadcrumb">Extract Data from selected Study Location Visits</span>
                        </td>
                        <td></td>
                    </tr>
                </table>
            </legend>

            <div class="row">
                <div class="span12">
                    <g:form>
                        <div class="well well-small">
                            <h4>I am interested in:</h4>
                            <br/>
                            <table>
                                <g:each in="${questions}" var="question">
                                    <tr>
                                        <g:set var="isChecked" value="${selectedQuestionIds?.contains(question.id.toString())}" />
                                        <td style="width:30px; vertical-align: middle"><g:checkBox style="display: inline-block; vertical-align: middle; margin-top:0" name="questionId" checked="${isChecked}" value="${question.id}" /></td>
                                        <td>${question.text}</td>
                                    </tr>
                                </g:each>
                            </table>
                        </div>

                        <g:link class="btn btn-small" event="cancel">Cancel</g:link>
                        <g:link class="btn btn-small" event="back"><i class="icon-chevron-left"></i>&nbsp;Previous</g:link>
                        <g:submitButton name="continue" class="btn btn-small btn-primary" value="Next" />
                    </g:form>
                </div>
            </div>
        </div>
    </body>
</html>
