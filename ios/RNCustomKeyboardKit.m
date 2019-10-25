#import <React/RCTRootView.h>
#import "RNCustomKeyboardKit.h"
#import "RCTBridge+Private.h"
#import "RCTUIManager.h"
#import <RCTText/RCTSinglelineTextInputView.h>

@implementation RNCustomKeyboardKit

@synthesize bridge = _bridge;

- (dispatch_queue_t)methodQueue
{
	return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(CustomKeyboardKit)

RCT_EXPORT_METHOD(install:(nonnull NSNumber *)reactTag withType:(nonnull NSString *)keyboardType)
{
  RCTSinglelineTextInputView *view = (RCTSinglelineTextInputView*)[_bridge.uiManager viewForReactTag:reactTag];
  UITextField *textView = view.backedTextInputView;
  textView.inputView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 0, 0)];
  textView.inputAccessoryView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 0, 0)];
  [textView reloadInputViews];
}

RCT_EXPORT_METHOD(uninstall:(nonnull NSNumber *)reactTag)
{
  RCTSinglelineTextInputView *view = [_bridge.uiManager viewForReactTag:reactTag];
  UITextField *textView = view.backedTextInputView;
  textView.inputView = nil;
  textView.inputAccessoryView = nil;
  [textView reloadInputViews];
}

RCT_EXPORT_METHOD(insertText:(nonnull NSNumber *)reactTag withText:(NSString*)text) {
  RCTSinglelineTextInputView *view = (RCTSinglelineTextInputView*)[_bridge.uiManager viewForReactTag:reactTag];
  UITextField *textView = view.backedTextInputView;

  [textView replaceRange:textView.selectedTextRange withText:text];
}

RCT_EXPORT_METHOD(setText:(nonnull NSNumber *)reactTag withText:(NSString*)text) {
  RCTSinglelineTextInputView *view = (RCTSinglelineTextInputView*)[_bridge.uiManager viewForReactTag:reactTag];
  UITextField *textView = view.backedTextInputView;
  [textView setText:text];
  UITextRange* range = textView.selectedTextRange;
  [textView replaceRange:range withText:@""];
}

RCT_EXPORT_METHOD(getText:(nonnull NSNumber *)reactTag  resolver:(RCTPromiseResolveBlock)resolve
                rejecter:(RCTPromiseRejectBlock)reject) {
  RCTSinglelineTextInputView *view = (RCTSinglelineTextInputView*)[_bridge.uiManager viewForReactTag:reactTag];
  UITextField *textView = view.backedTextInputView;
  resolve(textView.text);
}

RCT_EXPORT_METHOD(hideStandardKeyboard:(nonnull NSNumber *)reactTag) {
  RCTSinglelineTextInputView *view = (RCTSinglelineTextInputView*)[_bridge.uiManager viewForReactTag:reactTag];
  UITextField *textView = view.backedTextInputView;
  textView.inputView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 0, 0)];
  [textView reloadInputViews];
}

RCT_EXPORT_METHOD(backSpace:(nonnull NSNumber *)reactTag) {
  RCTSinglelineTextInputView *view = (RCTSinglelineTextInputView*)[_bridge.uiManager viewForReactTag:reactTag];
  UITextField *textView = view.backedTextInputView;
    
  UITextRange* range = textView.selectedTextRange;
  if ([textView comparePosition:range.start toPosition:range.end] == 0) {
    range = [textView textRangeFromPosition:[textView positionFromPosition:range.start offset:-1] toPosition:range.start];
  }
  [textView replaceRange:range withText:@""];
}

RCT_EXPORT_METHOD(doDelete:(nonnull NSNumber *)reactTag) {
  UITextView *view = (UITextView*)[_bridge.uiManager viewForReactTag:reactTag];

  UITextRange* range = view.selectedTextRange;
  if ([view comparePosition:range.start toPosition:range.end] == 0) {
    range = [view textRangeFromPosition:range.start toPosition:[view positionFromPosition: range.start offset: 1]];
  }
  [view replaceRange:range withText:@""];
}

RCT_EXPORT_METHOD(moveLeft:(nonnull NSNumber *)reactTag) {
  UITextView *view = (UITextView*)[_bridge.uiManager viewForReactTag:reactTag];

  UITextRange* range = view.selectedTextRange;
  UITextPosition* position = range.start;

  if ([view comparePosition:range.start toPosition:range.end] == 0) {
      position = [view positionFromPosition:position offset:-1];
  }

  view.selectedTextRange = [view textRangeFromPosition: position toPosition:position];
}

RCT_EXPORT_METHOD(moveRight:(nonnull NSNumber *)reactTag) {
  UITextView *view = (UITextView*)[_bridge.uiManager viewForReactTag:reactTag];

  UITextRange* range = view.selectedTextRange;
  UITextPosition* position = range.end;

  if ([view comparePosition:range.start toPosition:range.end] == 0) {
    position = [view positionFromPosition: position offset: 1];
  }

  view.selectedTextRange = [view textRangeFromPosition: position toPosition:position];
}

RCT_EXPORT_METHOD(switchSystemKeyboard:(nonnull NSNumber*) reactTag) {
  UITextView *view = [_bridge.uiManager viewForReactTag:reactTag];
  UIView* inputView = view.inputView;
  view.inputView = nil;
  [view reloadInputViews];
  view.inputView = inputView;
}

@end
