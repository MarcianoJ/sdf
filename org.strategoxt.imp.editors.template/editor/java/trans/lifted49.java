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

@SuppressWarnings("all") final class lifted49 extends Strategy 
{ 
  public static final lifted49 instance = new lifted49();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    Fail221:
    { 
      IStrategoTerm e_10730 = null;
      if(term.getTermType() != IStrategoTerm.TUPLE || term.getSubtermCount() != 2)
        break Fail221;
      e_10730 = term.getSubterm(0);
      IStrategoTerm arg39 = term.getSubterm(1);
      term = aux_$Current$File_0_1.instance.invoke(context, e_10730, arg39);
      if(term == null)
        break Fail221;
      if(true)
        return term;
    }
    return null;
  }
}