/**
 * ===================================================================
 *
 * Copyright (c) 2003 Ludovic Dubost, All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details, published at
 * http://www.gnu.org/copyleft/lesser.html or in lesser.txt in the
 * root folder of this distribution.
 *
 * Created by
 * User: Ludovic Dubost
 * Date: 26 nov. 2003
 * Time: 21:00:05
 */
package com.xpn.xwiki.render;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.XWiki;
import com.xpn.xwiki.doc.XWikiDocInterface;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.struts.MessageTool;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.apache.velocity.app.Velocity;

import javax.servlet.ServletContext;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.io.StringReader;
import java.io.StringWriter;

public class XWikiVelocityRenderer implements XWikiRenderer {

    public XWikiVelocityRenderer() {
          try {
              Velocity.init();
          } catch (Exception e) {
              e.printStackTrace();  //To change body of catch statement use Options | File Templates.
          }
    }

    public String render(String content, XWikiDocInterface doc, XWikiContext context) {
        StringWriter writer = new StringWriter();
        String name = doc.getFullName();
        content = context.getUtil().substitute("s/#include\\(/\\\\#include\\(/go", content);
        VelocityContext vcontext = prepareContext(context);
        vcontext.put("doc", new Document(doc, context));
        return evaluate(content, name, vcontext);
    }

    public static VelocityContext prepareContext(XWikiContext context) {
        VelocityContext vcontext = new VelocityContext();
        vcontext.put("xwiki", new XWiki(context.getWiki(), context));
        vcontext.put("request", context.getRequest());
        vcontext.put("response", context.getResponse());
        vcontext.put("servlet", context.getServlet());
        vcontext.put("context", context);

        MessageTool msg =  new MessageTool();
        HttpServlet servlet = context.getServlet();
        if (servlet!=null) {
            msg.init(new ChainedContext(vcontext, context.getRequest(),
                 context.getResponse(), (servlet==null) ? null : servlet.getServletContext()));
            vcontext.put("msg", msg);
        }

        // Put the Velocity Context in the context
        // so that includes can use it..
        context.put("vcontext", vcontext);
        return vcontext;
    }

    public static String evaluate(String content, String name, VelocityContext vcontext) {
        StringWriter writer = new StringWriter();
        try {
          boolean result =  Velocity.evaluate(vcontext, writer, name,
                                new StringReader(content));
          return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while parsing velocity page " + name + ": " + e.getMessage();
        }
    }
}
