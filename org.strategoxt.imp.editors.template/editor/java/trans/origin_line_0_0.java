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

@SuppressWarnings("all") public class origin_line_0_0 extends Strategy 
{ 
  public static origin_line_0_0 instance = new origin_line_0_0();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    Fail1890:
    { 
      IStrategoTerm v_32998 = null;
      IStrategoTerm q_33188 = null;
      q_33188 = term;
      term = context.invokePrimitive("SSL_EXT_origin_location", q_33188, NO_STRATEGIES, new IStrategoTerm[]{term});
      if(term == null)
        break Fail1890;
      if(term.getTermType() != IStrategoTerm.TUPLE || term.getSubtermCount() != 4)
        break Fail1890;
      v_32998 = term.getSubterm(0);
      term = v_32998;
      if(true)
        return term;
    }
    context.push("origin_line_0_0");
    context.popOnFailure();
    return null;
  }
}