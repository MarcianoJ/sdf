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

@SuppressWarnings("all") public class verify_1_1 extends Strategy 
{ 
  public static verify_1_1 instance = new verify_1_1();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy w_9749, IStrategoTerm ref_v_9749)
  { 
    TermReference v_9749 = new TermReference(ref_v_9749);
    context.push("verify_1_1");
    Fail380:
    { 
      lifted247 lifted2470 = new lifted247();
      lifted2470.w_9749 = w_9749;
      lifted2470.v_9749 = v_9749;
      term = contracts_1_0.instance.invoke(context, term, lifted2470);
      if(term == null)
        break Fail380;
      context.popOnSuccess();
      if(true)
        return term;
    }
    context.popOnFailure();
    return null;
  }
}