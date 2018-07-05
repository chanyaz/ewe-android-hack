package com.expedia.layouttestandroid.util

object LayoutFile {

    //TODO HTML Raw file should be used instead of this
    val layoutIndexFile = """
<!DOCTYPE html>
<html>

<head>
    <title>Layout Test Results</title>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.3/jquery-ui.min.js"></script>
    <script src="https://cl.ly/3t2y1O1O0q3M/download/default.jss"></script>
    <link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.3/themes/smoothness/jquery-ui.css" />
    <link rel="stylesheet" href="https://cl.ly/1B1h321f0p1G/download/default.css">
    <script src="https://cl.ly/0b2N3t1J411s/download/jquery.tmpl.min.jss"></script>

    <script type="text/javascript">
           var jsonData = [];
    </script>

    {script-tags}

    <script type="text/javascript">
        ${'$'}(document).ready(function() {
            for (i = 0; i < jsonData.length; i++) {
                ${'$'}("#screenshotTemplate").tmpl(jsonData[i]).appendTo("screenshot-body");
            }
        });
    </script>

    <script id="screenshotTemplate" type="text/html">
        <div class="screenshot alternate">
            <div class="screenshot_name"><span class="demphasize">${'$'}{appPackageName}/</span>${'$'}{testClass}</div>
            <div class="screenshot_description">${'$'}{testName}</div>
            <div class="flex-wrapper">
                <div class="img-wrapper">
                    <img src="${'$'}{testName}.png" style="width:${'$'}{size.width/4}px;height:${'$'}{size.height/4}px;box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);"/>
                    <div class="hierarchy-overlay">
                        <div class="hierarchy-node" style="left:${'$'}{hierarchyDump.left/4}px;top:${'$'}{hierarchyDump.top/4}px;width:${'$'}{hierarchyDump.width/4 - 4}px;height:${'$'}{hierarchyDump.height/4 - 4}px;" id="node-${'$'}{hierarchyDump.class.replace(/\./g, '-')}-${'$'}{hierarchyDump.hashCode}"></div>
                        {{tmpl(hierarchyDump) "#hierarchyDumpTemplate"}}
                    </div>
                </div>
                <div class="command-wrapper">
                    <button class="toggle_dark">Toggle Dark Background</button>
                    <button class="toggle_hierarchy">Toggle View Hierarchy Overlay</button>
                    <h3>View Hierarchy</h3>
                    <div class="view-hierarchy">
                        <details target="#node-${'$'}{hierarchyDump.class.replace (/\./g, '-')}-${'$'}{hierarchyDump.hashCode}">
                            <summary>${'$'}{hierarchyDump.class} - ${'$'}{hierarchyDump.hashCode}</summary>
                            <ul>
                                <li><strong>height:</strong> ${'$'}{hierarchyDump.height}</li>
                                <li><strong>left:</strong> ${'$'}{hierarchyDump.left}</li>
                                <li><strong>top:</strong> ${'$'}{hierarchyDump.top}</li>
                                <li><strong>width:</strong> ${'$'}{hierarchyDump.width}</li>
                            </ul>
                            {{tmpl(hierarchyDump) "#hierarchyNodeTemplate"}}
                        </details>
                    </div>
                    <h3>Exceptions</h3>
                    <div class="view-hierarchy">
                        {{each layoutTestExceptions}}
                            <summary><strong>${'$'}{${'$'}value.message}</strong></summary>
                            <ul>
                            {{each ${'$'}value.views}}
                                <li class="error-view-hierarchy" style="list-style: disc" target="#node-${'$'}{${'$'}value.class.replace(/\./g, '-')}-${'$'}{${'$'}value.hashCode}">${'$'}{${'$'}value.class} - ${'$'}{${'$'}value.hashCode}</li>
                            {{/each}}
                            </ul>
                        {{/each}}
                    </div>
                </div>
            </div>
        </div>
        <div class="clearfix"></div>
        <hr/>
    </script>

    <script id="hierarchyDumpTemplate" type="text/html">
        {{each children}}
            <div class="hierarchy-node" style="left:${'$'}{${'$'}value.left/4}px;top:${'$'}{${'$'}value.top/4}px;width:${'$'}{${'$'}value.width/4 - 4}px;height:${'$'}{${'$'}value.height/4 - 4}px;" id="node-${'$'}{${'$'}value.class.replace(/\./g, '-')}-${'$'}{${'$'}value.hashCode}"></div>
            {{if typeof ${'$'}value.children !== "undefined"}}
                {{tmpl(${'$'}value) "#hierarchyDumpTemplate"}}
            {{/if}}
        {{/each}}
    </script>

    <script id="hierarchyNodeTemplate" type="text/html">
        {{each children}}
        <details target="#node-${'$'}{${'$'}value.class.replace(/\./g, '-')}-${'$'}{${'$'}value.hashCode}">
            <summary>${'$'}{${'$'}value.class} - ${'$'}{${'$'}value.hashCode}</summary>
            <ul>
                <li><strong>height:</strong> ${'$'}{${'$'}value.height}</li>
                <li><strong>left:</strong> ${'$'}{${'$'}value.left}</li>
                <li><strong>top:</strong> ${'$'}{${'$'}value.top}</li>
                <li><strong>width:</strong> ${'$'}{${'$'}value.width}</li>
            </ul>
            {{if typeof ${'$'}value.children !== "undefined"}}
                {{tmpl(${'$'}value) "#hierarchyNodeTemplate"}}
            {{/if}}

        </details>
        {{/each}}
    </script>

</head>

<body>

<screenshot-body></screenshot-body>

</body>

</html>
"""
}
