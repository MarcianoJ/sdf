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

@SuppressWarnings("all") final class lifted60 extends Strategy 
{ 
  public static final lifted60 instance = new lifted60();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    Fail210:
    { 
      IStrategoTerm a_10735 = null;
      IStrategoTerm i_10735 = null;
      if(term.getTermType() != IStrategoTerm.TUPLE || term.getSubtermCount() != 2)
        break Fail210;
      i_10735 = term.getSubterm(0);
      IStrategoTerm arg46 = term.getSubterm(1);
      a_10735 = arg46;
      term = aux_$Is$Imported_0_2.instance.invoke(context, i_10735, arg46, a_10735);
      if(term == null)
        break Fail210;
      if(true)
        return term;
    }
    return null;
  }
}