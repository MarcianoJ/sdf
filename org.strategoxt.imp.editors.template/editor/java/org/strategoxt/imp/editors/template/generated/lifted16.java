package org.strategoxt.imp.editors.template.generated;

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

@SuppressWarnings("all") final class lifted16 extends Strategy 
{ 
  TermReference g_9706;

  TermReference h_9706;

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    Fail1005:
    { 
      if(g_9706.value == null)
        break Fail1005;
      term = map_1_0.instance.invoke(context, g_9706.value, template_to_prettyprint_strategy_0_0.instance);
      if(term == null)
        break Fail1005;
      if(h_9706.value == null)
        h_9706.value = term;
      else
        if(h_9706.value != term && !h_9706.value.match(term))
          break Fail1005;
      if(true)
        return term;
    }
    return null;
  }
}