package com.douban.book

import com.douban.base.{Constant, DoubanFragment, DoubanActivity}
import android.view.{WindowManager, View, ViewGroup, LayoutInflater}
import android.os.Bundle
import android.widget.EditText

import org.scaloid.common._
import scala.concurrent._
import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.douban.models.{AnnotationPosted, Book}
import scala.util.Success
import scala.util.Success
import com.douban.models.AnnotationPosted
import ExecutionContext.Implicits.global

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 9/30/13 11:51 AM
 * @version 1.0
 */
class AddNoteActivity extends DoubanActivity {
  var bookPage=""
  var chapter=""
  var public=true
  override def onCreate(b: Bundle){
    super.onCreate(b)
    setContentView(R.layout.add_note_container)
  }

  def submit(v:View){
    findViewById(R.id.bookPage) match{
      case bp:EditText=>{
        bookPage=bp.getText.toString
        chapter=find[EditText](R.id.chapter_name).getText.toString
        fragmentManager.beginTransaction().replace(R.id.add_note_container,new AddNoteFragment).commit()
      }
      case _=> future {
        getIntent.getLongExtra(Constant.BOOK_ID,0) match {
          case bookId:Long if bookId>0 => {
            val a=new AnnotationPosted(find[EditText](R.id.note_input).text.toString,bookPage.toInt,chapter,if(public) "public" else "private")
            Book.postAnnotation(bookId,a)
          }
          case _=>None
        }
      }onComplete{
        case Success(Some(a))=>runOnUiThread{
          toast(R.string.annotation_added)
          this.onBackPressed()
        }
        case _=> toast(R.string.annotation_fails_to_add)
      }
    }
  }
  def editChapter(v:View){
    fragmentManager.beginTransaction().replace(R.id.add_note_container,new AddChapterFragment()).addToBackStack(null).commit()
  }

  def toggleKeyboard(v:View){
    val acceptingText: Boolean = getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager].isAcceptingText
    toggleBackGround(acceptingText,R.id.key_board,(R.drawable.keyboard_down,R.drawable.keyboard_up))
    if(acceptingText)     getWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    else    getWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
  }

  def checkPrivacy(v: View) {
    public = toggleBackGround(public, v, (R.drawable.private_icon, R.drawable.public_icon))
  }
}

class AddChapterFragment extends DoubanFragment[AddNoteActivity]{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.note_add_chapter,container,false)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    getThisActivity.replaceActionBar(R.layout.header_tag,"添加页码/章节名")
    setViewValue(R.id.bookPage,getThisActivity.bookPage)
    setViewValue(R.id.chapter_name,getThisActivity.chapter)
  }
}

class AddNoteFragment extends DoubanFragment[AddNoteActivity]{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, b: Bundle): View = inflater.inflate(R.layout.note_add,container,false)

  override def onActivityCreated(b: Bundle) {
    super.onActivityCreated(b)
    getThisActivity.replaceActionBar(R.layout.header_edit_note,if(getThisActivity.bookPage.isEmpty) getThisActivity.chapter else getThisActivity.bookPage)
  }
}

