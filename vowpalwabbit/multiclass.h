/*
Copyright (c) by respective owners including Yahoo!, Microsoft, and
individual contributors. All rights reserved.  Released under a BSD
license as described in the file LICENSE.
 */
#pragma once
#include "label_parser.h"
#include "v_array.h"

struct example;
struct vw;

namespace MULTICLASS
{
  struct label_t {
    uint32_t label;
    float weight;
    v_array<float> partial_predictions;
  };
  
  extern label_parser mc_label;
  
  void finish_example(vw& all, example& ec);

  template <class T> void finish_example(vw& all, T&, example& ec) { finish_example(all, ec); }

  inline bool label_is_test(label_t* ld)
  { return ld->label == (uint32_t)-1; }
}
