package trans;

import org.strategoxt.stratego_lib.*;
import org.strategoxt.stratego_lib.*;
import org.strategoxt.stratego_sglr.*;
import org.strategoxt.stratego_gpp.*;
import org.strategoxt.stratego_xtc.*;
import org.strategoxt.stratego_aterm.*;
import org.strategoxt.stratego_sdf.*;
import org.strategoxt.strc.*;
import org.strategoxt.imp.editors.template.generated.*;
import org.strategoxt.java_front.*;
import org.strategoxt.lang.*;
import org.spoofax.interpreter.terms.*;
import static org.strategoxt.lang.Term.*;
import org.spoofax.interpreter.library.AbstractPrimitive;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

@SuppressWarnings("all") public class parse_templatelang_string_0_0 extends Strategy 
{ 
  public static parse_templatelang_string_0_0 instance = new parse_templatelang_string_0_0();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    context.push("parse_templatelang_string_0_0");
    Fail1848:
    { 
      IStrategoTerm l_32992 = null;
      IStrategoTerm m_32992 = null;
      l_32992 = term;
      m_32992 = trans.constTemplateLangTbl0;
      term = parse_string_1_1.instance.invoke(context, l_32992, strsglr_report_parse_error_0_0.instance, m_32992);
      if(term == null)
        break Fail1848;
      context.popOnSuccess();
      if(true)
        return term;
    }
    context.popOnFailure();
    return null;
  }
}